echo "####################################"
echo "# Building test-2 and dependencies #"
echo "####################################"
mvn clean install -Pall -pl uk.co.optimisticpanda:test-2 -am -DskipTests

echo "###################################"
echo "# Building test-2 and dependents  #"
echo "###################################"
mvn clean install -Pall -pl uk.co.optimisticpanda:test-2 -amd -DskipTests



