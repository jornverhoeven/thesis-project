DIRECTORY=./output/graphs

#for i in $DIRECTORY/*.mmd; do
#    xbase=${i##*/}
#    xpref=${xbase%.*}
#    npx -p @mermaid-js/mermaid-cli mmdc -i $i -o $DIRECTORY/${xpref%.*}.png
#    rm $i;
#done

convert -delay 10 -loop 0 $(find $DIRECTORY -iname "attackGraph-node-a-*.png") $DIRECTORY/attackGraph-node-a.gif
convert -delay 10 -loop 0 $DIRECTORY/attackGraph-node-b-%d.png[1-9] $DIRECTORY/attackGraph-node-b.gif
convert -delay 10 -loop 0 $DIRECTORY/attackGraph-node-c-%d.png[1-9] $DIRECTORY/attackGraph-node-c.gif
convert -delay 10 -loop 0 $DIRECTORY/attackGraph-node-d-%d.png[1-9] $DIRECTORY/attackGraph-node-d.gif