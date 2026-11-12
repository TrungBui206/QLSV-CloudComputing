#!/usr/bin/env bash
# Chạy lệnh trên một node từ AWS CloudShell (SSH qua EC2 Instance Connect, không cần file khóa lâu dài):
#   ./aws-node.sh cp kubectl get nodes -o wide
#   ./aws-node.sh worker1            # mở phiên SSH
#   ./aws-node.sh start|stop|status  # bật / tắt / xem cả 3 máy
set -euo pipefail
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-ap-southeast-2}"
FILTER=(--filters Name=tag:Project,Values=k8s-midterm Name=instance-state-name,Values=pending,running,stopping,stopped)

case "${1:-}" in
  status)
    aws ec2 describe-instances "${FILTER[@]}" --output table \
      --query 'Reservations[].Instances[].[Tags[?Key==`Name`]|[0].Value,InstanceId,State.Name,PrivateIpAddress,PublicIpAddress]'
    exit 0 ;;
  start|stop)
    IDS=$(aws ec2 describe-instances "${FILTER[@]}" --query 'Reservations[].Instances[].InstanceId' --output text)
    aws ec2 "$1-instances" --instance-ids $IDS --query '*[].[InstanceId,CurrentState.Name]' --output text
    exit 0 ;;
  cp|worker1|worker2) ;;
  *) echo "Cach dung: aws-node.sh <cp|worker1|worker2> [lenh...] | start | stop | status"; exit 1 ;;
esac

NAME="$1"; shift
read -r ID IP SG <<< "$(aws ec2 describe-instances \
  --filters Name=tag:Name,Values="k8s-$NAME" Name=instance-state-name,Values=running \
  --query 'Reservations[0].Instances[0].[InstanceId,PublicIpAddress,SecurityGroups[0].GroupId]' --output text)"
[ "$ID" != "None" ] || { echo "k8s-$NAME chưa chạy"; exit 1; }

# Cho phép SSH từ IP hiện tại của CloudShell (IP này đổi theo phiên; trùng thì AWS báo lỗi và ta bỏ qua)
MYIP=$(curl -s https://checkip.amazonaws.com)
aws ec2 authorize-security-group-ingress --group-id "$SG" --protocol tcp --port 22 --cidr "$MYIP/32" > /dev/null 2>&1 || true

KEY="$HOME/.ssh/k8s-midterm"
[ -f "$KEY" ] || { mkdir -p "$HOME/.ssh"; ssh-keygen -q -t ed25519 -N '' -f "$KEY"; }
# Khóa công khai chỉ có hiệu lực 60 giây để mở kết nối
aws ec2-instance-connect send-ssh-public-key --instance-id "$ID" --instance-os-user ubuntu \
  --ssh-public-key "file://$KEY.pub" > /dev/null
exec ssh -i "$KEY" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=ERROR "ubuntu@$IP" "$@"
