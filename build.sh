#!/bin/bash

# Low Latency Stack Build Script

set -e

echo "🚀 Building Low Latency Stack..."

# Clean previous builds
echo "🧹 Cleaning previous builds..."
mvn clean

# Compile and run tests
echo "🔨 Compiling and running tests..."
mvn compile test

# Generate test coverage report
echo "📊 Generating test coverage report..."
mvn jacoco:report

# Package the application
echo "📦 Packaging application..."
mvn package

# Run performance benchmark
echo "⚡ Running performance benchmark..."
java -Xmx2g -XX:+UseG1GC -jar target/low-latency-stack-1.0.0.jar

echo "✅ Build completed successfully!"
echo ""
echo "📋 Build artifacts:"
echo "   - JAR file: target/low-latency-stack-1.0.0.jar"
echo "   - Test reports: target/surefire-reports/"
echo "   - Coverage report: target/site/jacoco/index.html"
echo ""
echo "🏃‍♂️ To run the application:"
echo "   java -jar target/low-latency-stack-1.0.0.jar"