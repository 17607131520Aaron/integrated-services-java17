#!/bin/bash

# 企业级集成服务部署脚本
# 支持本地、测试、生产环境部署

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_debug() {
    echo -e "${BLUE}[DEBUG]${NC} $1"
}

# 默认配置
ENVIRONMENT="dev"
SERVICE_NAME="integrated-services"
DOCKER_COMPOSE_FILE="docker-compose.yml"
HEALTH_CHECK_URL="http://localhost:8080/api/actuator/health"
MAX_WAIT_TIME=120

# 显示帮助信息
show_help() {
    echo "企业级集成服务部署脚本"
    echo
    echo "使用方法: $0 [命令] [选项]"
    echo
    echo "命令:"
    echo "  start       启动服务"
    echo "  stop        停止服务"
    echo "  restart     重启服务"
    echo "  status      查看服务状态"
    echo "  logs        查看服务日志"
    echo "  health      检查服务健康状态"
    echo "  clean       清理资源"
    echo
    echo "选项:"
    echo "  -e, --env ENV          指定环境 (dev|test|prod, 默认: dev)"
    echo "  -f, --file FILE        指定docker-compose文件"
    echo "  -w, --wait SECONDS     健康检查等待时间 (默认: 120秒)"
    echo "  -h, --help             显示帮助信息"
    echo
    echo "示例:"
    echo "  $0 start -e prod"
    echo "  $0 restart -f docker-compose.prod.yml"
    echo "  $0 logs -e test"
}

# 检查Docker环境
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        log_error "Docker服务未启动，请启动Docker服务"
        exit 1
    fi
    
    log_info "Docker环境检查通过"
}

# 等待服务健康
wait_for_health() {
    local url=$1
    local max_wait=$2
    local wait_time=0
    
    log_info "等待服务启动，健康检查URL: $url"
    
    while [ $wait_time -lt $max_wait ]; do
        if curl -f -s "$url" > /dev/null 2>&1; then
            log_info "服务健康检查通过"
            return 0
        fi
        
        echo -n "."
        sleep 5
        wait_time=$((wait_time + 5))
    done
    
    echo
    log_error "服务启动超时，健康检查失败"
    return 1
}

# 启动服务
start_service() {
    log_info "启动服务环境: $ENVIRONMENT"
    
    # 设置环境变量
    export SPRING_PROFILES_ACTIVE=$ENVIRONMENT
    
    # 创建必要的目录
    mkdir -p logs
    
    # 启动服务
    docker-compose -f "$DOCKER_COMPOSE_FILE" up -d
    
    if [ $? -eq 0 ]; then
        log_info "服务启动命令执行成功"
        
        # 等待服务健康
        if wait_for_health "$HEALTH_CHECK_URL" "$MAX_WAIT_TIME"; then
            log_info "服务启动成功！"
            show_service_info
        else
            log_error "服务启动失败，请检查日志"
            docker-compose -f "$DOCKER_COMPOSE_FILE" logs --tail=50
            exit 1
        fi
    else
        log_error "服务启动失败"
        exit 1
    fi
}

# 停止服务
stop_service() {
    log_info "停止服务..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" down
    
    if [ $? -eq 0 ]; then
        log_info "服务停止成功"
    else
        log_error "服务停止失败"
        exit 1
    fi
}

# 重启服务
restart_service() {
    log_info "重启服务..."
    stop_service
    sleep 3
    start_service
}

# 查看服务状态
show_status() {
    log_info "服务状态:"
    docker-compose -f "$DOCKER_COMPOSE_FILE" ps
    
    echo
    log_info "容器资源使用情况:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
}

# 查看服务日志
show_logs() {
    log_info "查看服务日志..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" logs -f --tail=100 "$SERVICE_NAME"
}

# 健康检查
health_check() {
    log_info "执行健康检查..."
    
    if curl -f -s "$HEALTH_CHECK_URL" | jq . 2>/dev/null; then
        log_info "服务健康状态: 正常"
        return 0
    else
        log_error "服务健康状态: 异常"
        return 1
    fi
}

# 清理资源
clean_resources() {
    log_warn "清理所有资源（包括数据卷）..."
    read -p "确认要清理所有资源吗？这将删除所有数据 (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose -f "$DOCKER_COMPOSE_FILE" down -v --remove-orphans
        docker system prune -f
        log_info "资源清理完成"
    else
        log_info "取消清理操作"
    fi
}

# 显示服务信息
show_service_info() {
    echo
    log_info "服务访问信息:"
    echo "  应用地址: http://localhost:8080/api"
    echo "  API文档: http://localhost:8080/api/doc.html"
    echo "  监控面板: http://localhost:8080/api/actuator"
    echo "  数据库监控: http://localhost:8080/api/druid"
    echo "  Prometheus: http://localhost:9090"
    echo "  Grafana: http://localhost:3000 (admin/admin123)"
    echo
    echo "  默认账户: admin/admin123"
}

# 解析参数
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -e|--env)
                ENVIRONMENT="$2"
                shift 2
                ;;
            -f|--file)
                DOCKER_COMPOSE_FILE="$2"
                shift 2
                ;;
            -w|--wait)
                MAX_WAIT_TIME="$2"
                shift 2
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                break
                ;;
        esac
    done
}

# 主函数
main() {
    local command=""
    
    # 解析命令
    if [ $# -gt 0 ]; then
        command=$1
        shift
    fi
    
    # 解析参数
    parse_args "$@"
    
    # 检查Docker环境
    check_docker
    
    # 执行命令
    case $command in
        start)
            start_service
            ;;
        stop)
            stop_service
            ;;
        restart)
            restart_service
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        health)
            health_check
            ;;
        clean)
            clean_resources
            ;;
        ""|help)
            show_help
            ;;
        *)
            log_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
