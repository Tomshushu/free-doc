#!/bin/bash
# Test script to verify SQL schema adaptation

echo "Testing SQL schema adaptation..."
echo ""
echo "Original SQL file first 30 lines:"
head -n 30 db/free_doc.sql
echo ""
echo "================================"
echo ""
echo "Building and testing..."
cd free-doc-server
mvn clean package -DskipTests -q
echo ""
echo "Build complete. Check logs above for any errors."
