PROJECT=skiff-api-plane-23
COMMIT="$(git rev-parse HEAD)"

wget -O yq https://github.com/mikefarah/yq/releases/download/v4.30.6/yq_linux_amd64
chmod +x yq

echo Sonar link: "https://sonar-hy.netease.com/project/issues?id=$PROJECT&pullRequest=$COMMIT"
TIMES=15
for i in `seq 1 $TIMES`; do
  RESP="$(curl -u 4c70c4b1e65ba13757239d6fea4cd5e548ad3e22: "https://sonar-hy.netease.com/api/measures/component?pullRequest=$COMMIT&component=$PROJECT&metricKeys=new_blocker_violations,new_critical_violations,new_major_violations")"
  STATS="$(echo "$RESP" | ./yq -P '.component.measures[]|{.metric: .period.value}')"
  echo "$RESP"
  echo "$STATS"

  BLOCKER=$(echo "$STATS" | ./yq '.new_blocker_violations')
  CRITICAL=$(echo "$STATS" | ./yq '.new_critical_violations')

  if [[ $BLOCKER != 0 || $CRITICAL != 0 ]]; then
    if echo "$RESP" | ./yq -P '.errors[0].msg' | grep -q "not found"; then
      echo sonar result not found, retrying...
      sleep 60
    else
      echo ERROR: There is new issue.
      exit 1
    fi
  else
    exit 0
  fi
done

echo sonar result not found, after $TIMES tries...
exit 1
