#!/usr/bin/env bash
# Tạo 3 máy EC2 (cp, worker1, worker2) cho cluster. Chạy trong AWS CloudShell:
#   ./aws-create-nodes.sh
# Mỗi máy tự chạy node-setup.sh khi khởi động lần đầu (qua user-data), mất khoảng 2-3 phút.
# Chạy lại an toàn: máy hoặc Security Group đã có thì bỏ qua.
set -euo pipefail
cd "$(dirname "$0")"
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-ap-southeast-2}"
TYPE="${TYPE:-c7i-flex.large}"      # 2 vCPU, 4 GB RAM (kubeadm cần tối thiểu 2 vCPU, 2 GB)
SG_NAME="k8s-midterm-sg"

VPC=$(aws ec2 describe-vpcs --filters Name=is-default,Values=true --query 'Vpcs[0].VpcId' --output text)
SUBNET=$(aws ec2 describe-subnets --filters Name=vpc-id,Values="$VPC" Name=default-for-az,Values=true \
  --query 'sort_by(Subnets,&AvailabilityZone)[0].SubnetId' --output text)
AMI=$(aws ssm get-parameter --name /aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id \
  --query Parameter.Value --output text)
echo "VPC=$VPC SUBNET=$SUBNET AMI=$AMI TYPE=$TYPE"

SG=$(aws ec2 describe-security-groups --filters Name=group-name,Values="$SG_NAME" Name=vpc-id,Values="$VPC" \
  --query 'SecurityGroups[0].GroupId' --output text)
if [ "$SG" = "None" ]; then
  SG=$(aws ec2 create-security-group --group-name "$SG_NAME" --vpc-id "$VPC" \
    --description "Cluster Kubernetes giua ky: cp, worker1, worker2" --query GroupId --output text)
  # Các node trong cluster gọi nhau tự do (API server 6443, kubelet 10250, Flannel VXLAN 8472/udp...)
  aws ec2 authorize-security-group-ingress --group-id "$SG" --protocol=-1 --source-group "$SG" > /dev/null
  # Ứng dụng: NodePort 30080 mở cho mọi nơi
  aws ec2 authorize-security-group-ingress --group-id "$SG" --protocol tcp --port 30080 --cidr 0.0.0.0/0 > /dev/null
  # SSH: chỉ từ dịch vụ EC2 Instance Connect (nút Connect trên Console)
  PL=$(aws ec2 describe-managed-prefix-lists \
    --filters Name=prefix-list-name,Values="com.amazonaws.$AWS_DEFAULT_REGION.ec2-instance-connect" \
    --query 'PrefixLists[0].PrefixListId' --output text)
  aws ec2 authorize-security-group-ingress --group-id "$SG" \
    --ip-permissions "IpProtocol=tcp,FromPort=22,ToPort=22,PrefixListIds=[{PrefixListId=$PL}]" > /dev/null
  echo "Đã tạo Security Group $SG"
else
  echo "Security Group đã có: $SG"
fi

for NAME in cp worker1 worker2; do
  ID=$(aws ec2 describe-instances \
    --filters Name=tag:Name,Values="k8s-$NAME" Name=instance-state-name,Values=pending,running,stopping,stopped \
    --query 'Reservations[0].Instances[0].InstanceId' --output text)
  if [ "$ID" != "None" ]; then echo "k8s-$NAME đã có: $ID"; continue; fi
  UD=$(mktemp)
  { echo '#!/bin/bash'; echo "set -- $NAME"; tail -n +2 node-setup.sh; } > "$UD"
  ID=$(aws ec2 run-instances --image-id "$AMI" --instance-type "$TYPE" --subnet-id "$SUBNET" \
    --security-group-ids "$SG" --associate-public-ip-address --user-data "file://$UD" \
    --block-device-mappings 'DeviceName=/dev/sda1,Ebs={VolumeSize=20,VolumeType=gp3}' \
    --metadata-options HttpTokens=required \
    --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=k8s-$NAME},{Key=Project,Value=k8s-midterm}]" \
    --query 'Instances[0].InstanceId' --output text)
  rm -f "$UD"
  echo "Đã tạo k8s-$NAME: $ID"
done

echo
aws ec2 describe-instances --filters Name=tag:Project,Values=k8s-midterm \
  Name=instance-state-name,Values=pending,running,stopping,stopped \
  --query 'Reservations[].Instances[].[Tags[?Key==`Name`]|[0].Value,InstanceId,State.Name,PrivateIpAddress,PublicIpAddress]' \
  --output table
