#!/bin/bash

# 企业级集成服务构建脚本
# 用于本地开发和CI/CD环境

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# 检查环境
check_environment() {
    log_info "检查构建环境..."
    
    # 检查Java版本
    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请安装JDK 17+"
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ]; then
        log_error "Java版本过低，需要JDK 17+，当前版本: $java_version"
        exit 1
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装，请安装Maven 3.8+"
        exit 1
    fi
    
    log_info "环境检查通过"
}

# 清理构建产物
clean_build() {
    log_info "清理构建产物..."
    mvn clean
    rm -rf logs/
    log_info "清理完成"
}

# 运行测试
run_tests() {
    log_info "运行单元测试..."
    mvn test
    log_info "测试完成"
}

# 构建应用
build_application() {
    log_info "构建应用..."
    mvn package -DskipTests
    
    if [ $? -eq 0 ]; then
        log_info "构建成功"
        ls -la target/*.jar
    else
        log_error "构建失败"
        exit 1
    fi
}

# 构建Docker镜像
build_docker_image() {
    log_info "构建Docker镜像..."
    
    IMAGE_NAME="integrated-services"
    IMAGE_TAG="latest"
    
    if [ ! -z "$1" ]; then
        IMAGE_TAG="$1"
    fi
    
    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
    
    if [ $? -eq 0 ]; then
        log_info "Docker镜像构建成功: ${IMAGE_NAME}:${IMAGE_TAG}"
        docker images | grep ${IMAGE_NAME}
    else
        log_error "Docker镜像构建失败"
        exit 1
    fi
}

# 主函数
main() {
    log_info "开始构建企业级集成服务..."
    
    # 解析参数
    SKIP_TESTS=false
    BUILD_DOCKER=false
    DOCKER_TAG="latest"
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --docker)
                BUILD_DOCKER=true
                shift
                ;;
            --tag)
                DOCKER_TAG="$2"
                shift 2
                ;;
            --help)
                echo "使用方法: $0 [选项]"
                echo "选项:"
                echo "  --skip-tests    跳过单元测试"
                echo "  --docker        构建Docker镜像"
                echo "  --tag TAG       指定Docker镜像标签 (默认: latest)"
                echo "  --help          显示帮助信息"
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                exit 1
                ;;
        esac
    done
    
    # 执行构建步骤
    check_environment
    clean_build
    
    if [ "$SKIP_TESTS" = false ]; then
        run_tests
    else
        log_warn "跳过单元测试"
    fi
    
    build_application
    
    if [ "$BUILD_DOCKER" = true ]; then
        build_docker_image "$DOCKER_TAG"
    fi
    
    log_info "构建完成！"
    
    # 显示构建结果
    echo
    log_info "构建结果:"
    echo "  JAR文件: $(ls target/*.jar)"
    if [ "$BUILD_DOCKER" = true ]; then
        echo "  Docker镜像: integrated-services:$DOCKER_TAG"
    fi
}

# 执行主函数
main "$@"
