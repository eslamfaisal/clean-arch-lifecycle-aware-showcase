#!/bin/bash

# ChatFragment UI Tests Runner
# Runs comprehensive UI tests for ChatFragment with proper Hilt setup

echo "🚀 Running ChatFragment UI Tests..."
echo "=================================="

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to run specific test class
run_test_class() {
    local test_class=$1
    local description=$2
    
    echo -e "\n${BLUE}📱 Running: $description${NC}"
    echo "Class: $test_class"
    echo "----------------------------------------"
    
    ./gradlew connectedAndroidTest \
        -Pandroid.testInstrumentationRunnerArguments.class=$test_class \
        --info
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $description - PASSED${NC}"
    else
        echo -e "${RED}❌ $description - FAILED${NC}"
        return 1
    fi
}

# Clean build first
echo -e "${BLUE}🧹 Cleaning project...${NC}"
./gradlew clean

# Run comprehensive UI tests
run_test_class "com.eslam.palmoutsource.presentation.chat.ChatFragmentHiltTest" \
    "Comprehensive ChatFragment UI Tests"

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Comprehensive tests failed, skipping crash fix tests${NC}"
    exit 1
fi

# Run lifecycle crash fix tests
run_test_class "com.eslam.palmoutsource.presentation.chat.ChatFragmentLifecycleCrashFixTest" \
    "Lifecycle Crash Fix Validation Tests"

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Crash fix tests failed${NC}"
    exit 1
fi

# Run all ChatFragment tests together
echo -e "\n${BLUE}🎯 Running All ChatFragment Tests Together...${NC}"
echo "=============================================="

./gradlew connectedAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.package=com.eslam.palmoutsource.presentation.chat \
    --info

if [ $? -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ALL CHATFRAGMENT UI TESTS PASSED!${NC}"
    echo -e "${GREEN}✅ Comprehensive UI functionality validated${NC}"
    echo -e "${GREEN}✅ Lifecycle crash fix confirmed working${NC}"
    echo -e "${GREEN}✅ Production-ready ChatFragment verified${NC}"
else
    echo -e "\n${RED}❌ Some tests failed. Check logs above.${NC}"
    exit 1
fi

echo -e "\n${BLUE}📊 Test Summary:${NC}"
echo "• ChatFragmentHiltTest: Comprehensive UI testing"
echo "• ChatFragmentLifecycleCrashFixTest: Critical crash prevention"
echo "• All tests use proper Hilt DI infrastructure"
echo "• Tests validate viewLifecycleOwner fix"
echo -e "\n${GREEN}Ready for production! 🚀${NC}"
