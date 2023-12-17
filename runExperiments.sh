infra=complex-infra.yml
# infra=base.yml
#infra=single-node-infra.yml
# scenarios=(large)
#scenarios=(no-change risk-introduction growing unstable)
# scenarios=(unstable)
# scenarios=(growing)
# scenarios=(risk-introduction)
scenarios=(no-change)
#features=(local knowledge-sharing auctioning)
# features=(knowledge-sharing auctioning)
# features=(local)
# features=(knowledge-sharing)
 features=(auctioning)

CURRENT=$(pwd)

for s in ${scenarios[@]}; do
    for f in ${features[@]}; do
        rm -rf $CURRENT/output/$s/$f/graphs
        rm -rf $CURRENT/output/$s/$f/output.txt
        mkdir -p $CURRENT/output/$s/$f
        mkdir -p $CURRENT/output/$s/$f/graphs
        cd $CURRENT/output/$s/$f
        echo "Running experiment for scenario $s and feature $f"
        java -Dfile.encoding=UTF-8 -classpath /Users/jornverhoeven/dev/jornverhoeven/thesis/packages/core2/target/classes:/Users/jornverhoeven/.m2/repository/org/apache/logging/log4j/log4j-api/2.20.0/log4j-api-2.20.0.jar:/Users/jornverhoeven/.m2/repository/org/apache/logging/log4j/log4j-core/2.20.0/log4j-core-2.20.0.jar:/Users/jornverhoeven/.m2/repository/org/yaml/snakeyaml/2.0/snakeyaml-2.0.jar tech.jorn.adrian.experiment.ExperimentRunner $infra $s $f 2>&1 | tee -a $CURRENT/output/$s/$f/output.txt
    done
done

# Run mutliple times
# runs=(1 2 3 4 5)
# for s in ${runs[@]}; do
#     mkdir -p $CURRENT/output/multi-run/$s
#     mkdir -p $CURRENT/output/multi-run/$s/graphs
#     cd $CURRENT/output/multi-run/$s/
#     echo "Run $s"
#     java -Dfile.encoding=UTF-8 -classpath /Users/jornverhoeven/dev/jornverhoeven/thesis/packages/core2/target/classes:/Users/jornverhoeven/.m2/repository/org/apache/logging/log4j/log4j-api/2.20.0/log4j-api-2.20.0.jar:/Users/jornverhoeven/.m2/repository/org/apache/logging/log4j/log4j-core/2.20.0/log4j-core-2.20.0.jar:/Users/jornverhoeven/.m2/repository/org/yaml/snakeyaml/2.0/snakeyaml-2.0.jar tech.jorn.adrian.experiment.ExperimentRunner $infra no-change full 2>&1 | tee -a $CURRENT/output/multi-run/$s/output.txt
# done

# Small infra
# for f in ${features[@]}; do
#     mkdir -p $CURRENT/output/small/$f
#     mkdir -p $CURRENT/output/small/$f/graphs
#     cd $CURRENT/output/small/$f
#     echo "Running experiment for small infra and feature $f"
#     java -Dfile.encoding=UTF-8 -classpath /Users/jornverhoeven/dev/jornverhoeven/thesis/packages/core2/target/classes:/Users/jornverhoeven/.m2/repository/org/apache/logging/log4j/log4j-api/2.20.0/log4j-api-2.20.0.jar:/Users/jornverhoeven/.m2/repository/org/apache/logging/log4j/log4j-core/2.20.0/log4j-core-2.20.0.jar:/Users/jornverhoeven/.m2/repository/org/yaml/snakeyaml/2.0/snakeyaml-2.0.jar tech.jorn.adrian.experiment.ExperimentRunner $infra no-change $f 2>&1 | tee -a $CURRENT/output/small/$f/output.txt
# done

cd $CURRENT
echo "Creating new graphs"
node ./packages/thesis/createGraphs.js

cd packages/thesis
npm run build
