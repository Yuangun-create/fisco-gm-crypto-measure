#!/bin/bash

cd ~/fisco/nodes/127.0.0.1

# 创建密钥备份文件
BACKUP_FILE=~/fisco/encryption_keys_$(date +%Y%m%d_%H%M%S).txt
echo "====== FISCO BCOS 落盘加密密钥备份 ======" > $BACKUP_FILE
echo "生成时间: $(date)" >> $BACKUP_FILE
echo "Key Manager: 127.0.0.1:8150" >> $BACKUP_FILE
echo "==========================================" >> $BACKUP_FILE
echo "" >> $BACKUP_FILE

for i in {0..3}; do
  node="node$i"
  echo "===== 配置 $node ====="
  
  # 1. 生成明文 dataKey 并获取加密后的 cipherDataKey
  PLAIN_KEY=$(openssl rand -hex 32)
  echo "生成 plain_data_key: $PLAIN_KEY"
  
  RESULT=$(bash ~/fisco/key-manager/scripts/gen_data_secure_key.sh 127.0.0.1 8150 "$PLAIN_KEY")
  KEY=$(echo "$RESULT" | grep -oP 'cipher_data_key=\K.*')
  
  if [ -z "$KEY" ]; then
    echo "错误：无法生成 cipher_data_key"
    exit 1
  fi
  
  echo "获取 cipher_data_key: $KEY"
  
  # 记录到备份文件
  echo "[$node]" >> $BACKUP_FILE
  echo "plain_data_key=$PLAIN_KEY" >> $BACKUP_FILE
  echo "cipher_data_key=$KEY" >> $BACKUP_FILE
  echo "" >> $BACKUP_FILE
  
  # 2. 配置 config.ini（删除旧配置并添加新配置）
  # 删除已存在的 [storage_security] 段
  sed -i '/^\[storage_security\]/,/^$/d' $node/config.ini
  
  # 添加新配置
  cat >> $node/config.ini << EOF

[storage_security]
enable=true
key_manager_ip=127.0.0.1
key_manager_port=8150
cipher_data_key=$KEY
EOF
  
  echo "已更新 config.ini"
  
  # 3. 加密节点私钥（国密双证书）
  cp $node/conf/gmnode.key.bak $node/conf/gmnode.key
  cp $node/conf/origin_cert/node.key.bak $node/conf/origin_cert/node.key
  echo "加密 gmnode.key..."
  bash ~/fisco/key-manager/scripts/encrypt_node_key.sh 127.0.0.1 8150 $node/conf/gmnode.key $KEY
  
  echo "加密 origin_cert/node.key..."
  bash ~/fisco/key-manager/scripts/encrypt_node_key.sh 127.0.0.1 8150 $node/conf/origin_cert/node.key $KEY
  
  echo "✓ $node 配置完成"
  echo ""
done

echo "=========================================="
echo "✓ 所有节点配置完成！"
echo "✓ 密钥已备份到: $BACKUP_FILE"
echo "现在可以启动节点: bash start_all.sh"
echo "=========================================="
