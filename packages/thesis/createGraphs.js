const fs = require("node:fs");

const scenarios = {
    noChange: "no-change",
    introduceRisk: "risk-introduction",
    growing: "growing",
}
const featureSet = {
    full: "auctioning",
    knowledgeSharing: "knowledge-sharing",
    local: "local",
}

function main() {
    writeToTexFile("overall-damage", "no-change", createDamageTotal(scenarios.noChange));
    writeToTexFile("overall-damage", "introduce-risk", createDamageTotal(scenarios.introduceRisk));
    writeToTexFile("overall-damage", "growing", createDamageTotal(scenarios.growing));
}

main();

function createDamageTotal(scenario) {
    const full = loadCSV(`./output/${scenario}/${featureSet.full}/metrics.csv`);
    const knowledgeSharing = loadCSV(`./output/${scenario}/${featureSet.knowledgeSharing}/metrics.csv`);
    const local = loadCSV(`./output/${scenario}/${featureSet.local}/metrics.csv`);

    const limits = getLimits(
        full.timestamps, 
        [].concat(full['riskDamage-global'], knowledgeSharing['riskDamage-global'], local['riskDamage-global']));
    const data = [
        {
            label: "Auctioning",
            data: zipTimestamps(full.timestamps, full['riskDamage-global']),
        },
        {
            label: "Knowledge Sharing",
            data: zipTimestamps(knowledgeSharing.timestamps, knowledgeSharing['riskDamage-global']),
        },
        {
            label: "Local",
            data: zipTimestamps(local.timestamps, local['riskDamage-global']),
        },
    ]
    return pgfPlotTemplate(`Damage Total for scenario ${scenario}`, "Damage Total", data, limits);
}

function loadCSV(path) {
    const data = fs.readFileSync(path, "utf8");
    const lines = data.split("\n");

    return lines.map(line => line.split(";"))
        .reduce((acc, line) => Object.assign(acc, { [line[0]]: line.splice(1) }), {});
}

function getLimits(timestamps, data) {
    const _data = data.map(d => parseFloat(d));
    return {
        timestamp: {
            min: Math.min(...timestamps),
            max: Math.max(...timestamps),
        },
        data: {
            min: Math.min(..._data),
            max: Math.max(..._data),
        }
    }
}
function zipTimestamps(timestamps, data) {
    return timestamps.map((timestamp, index) => [timestamp, data[index]]);
}

function pgfPlotTemplate(title, yLabel, data, limits) {
    return `\\begin{tikzpicture}
\\begin{axis}[
    title={${title}},
    xlabel={Timestamp [s]},
    ylabel={${yLabel}},
    xmin=0, xmax=${Math.round(limits.timestamp.max / 5000) * 5000 + 5000},
    ymin=0, ymax=${Math.round(limits.data.max / 100) * 100 + 100},
    legend pos=north west,
    ymajorgrids=true,
    grid style=dashed,
]

${data.map((d, index) => createPlot(d, index)).join("\n")}
    
\\end{axis}
\\end{tikzpicture}`;
}

function createPlot(data, index) {
    return `\\addplot[
    color=${color(index)},
    mark=${mark(index)},
    ]
    coordinates {
    ${data.data.map(([t, d]) => d ? `(${t},${d.toString().replace(",",".")})` : "").join("")}
    };
    \\addlegendentry{${data.label}}`
}

function color(index) {
    const colors = ["blue", "red", "orange", "black", "green"];
    return colors[index];
}
function mark(index) {
    const marks = ["square", "triangle", "diamond", "pentagon", "star"];
    return marks[index];
}

function writeToTexFile(name, scenario, plot) {
    fs.writeFileSync(`./packages/thesis/src/graphs/${name}-${scenario}.tex`, plot);
}
