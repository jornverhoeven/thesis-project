infra=complex-infra.yml
scenarios=(no-change risk-introduction growing unstable)
# scenarios=(growing)
features=(local knowledge-sharing auctioning)

CURRENT=$(pwd)

for s in ${scenarios[@]}; do
    for f in ${features[@]}; do
        mkdir -p $CURRENT/output/$s/$f
        mkdir -p $CURRENT/output/$s/$f/graphs
        cd $CURRENT/output/$s/$f
        echo "Running experiment for scenario $s and feature $f"
        java -Dfile.encoding=UTF-8 -classpath /Users/jornverhoeven/dev/jornverhoeven/thesis/packages/core2/target/classes:/Users/jornverhoeven/.m2/repository/org/apache/logging/log4j/log4j-api/2.20.0/log4j-api-2.20.0.jar:/Users/jornverhoeven/.m2/repository/org/apache/logging/log4j/log4j-core/2.20.0/log4j-core-2.20.0.jar:/Users/jornverhoeven/.m2/repository/org/yaml/snakeyaml/2.0/snakeyaml-2.0.jar tech.jorn.adrian.experiment.ExperimentRunner $infra $s $f 2>&1 | tee -a $CURRENT/output/$s/$f/output.txt
    done
done
