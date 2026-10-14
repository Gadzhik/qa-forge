#!/usr/bin/env bash
# Проверка курса для сопровождающих: подставить эталонные решения во временную копию проекта
# и прогнать все проверки (*Check). Все проверки должны пройти.
#
#   tools/verify-solutions.sh             — всё, включая UI (нужен Chrome) и Docker-проверки
#   SKIP_DOCKER=1 tools/verify-solutions.sh — без проверок, требующих Docker (Kafka, Testcontainers)
set -euo pipefail
cd "$(dirname "$0")/.."
ROOT=$(pwd)
WORK=${WORK:-$(mktemp -d)}   # можно задать свою папку: WORK=/путь tools/verify-solutions.sh
echo "Временная копия: $WORK"
cp -r mvnw mvnw.cmd .mvn pom.xml shop-app portal exercises testlab "$WORK/"   # portal нужен: корневой pom перечисляет его модулем
cp -r solutions/exercises/. "$WORK/exercises/"
cp -r solutions/testlab/. "$WORK/testlab/"
cd "$WORK"
./mvnw -B -q install -Dmaven.test.skip=true -pl shop-app,exercises/test-support -am
EXCLUDE=""
if [ "${SKIP_DOCKER:-}" = "1" ]; then
  EXCLUDE="!KafkaEventsTestCheck,!PostgresIntegrationTestCheck"
fi
TESTS='*Check'
[ -n "$EXCLUDE" ] && TESTS="*Check,$EXCLUDE"
./mvnw -B -f exercises/pom.xml test "-Dtest=$TESTS" -Dsurefire.failIfNoSpecifiedTests=false
./mvnw -B -f testlab/pom.xml test
echo "✅ Все эталонные решения проходят проверки"
