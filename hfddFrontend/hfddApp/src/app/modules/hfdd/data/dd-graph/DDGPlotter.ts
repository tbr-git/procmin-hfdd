import * as d3 from "d3";
import { ScaleSequential } from "d3";
import { DDGEdgeEMD } from "./edge/DDGEdgeEMD";
import { DDGEdgeFlowsplit } from "./edge/DDGEdgeFlowsplit";
import { DDGEdgeInterset } from "./edge/DDGEdgeInterset";
import { DDGLogProbDonut } from "./auxelements/DDGPictoProb";
import { DDGraph } from "./DDGraph";
import { DDGVertex } from "./vertex/DDGVertex";
import { DDGVertexEMDTrace } from "./vertex/DDGVertexEMDTrace";
import { DDGVertexFlowSplit } from "./vertex/DDGVertexFlowSplit";
import { DDGVertexHSet } from "./vertex/DDGVertexHSet";

export class DDGPlotter {

    private actColorMap: d3.ScaleOrdinal<string, string, never>;

    /**
     * Color map for the coloring of the flows according to the costs
     */
    private costContrColorMap : ScaleSequential<string, never>;


    /**
     * Maximum cost contribution value.
     * Every EMD cost contribution value above this value will be considered 
     * maximium expensive (e.g., brightest color)
     */
    private maxFlowVal = 0.5

    public constructor() {
        this.actColorMap = d3.scaleOrdinal(d3.schemeCategory10);
        this.costContrColorMap = d3.scaleSequential()
            .domain([0, this.maxFlowVal])
            .interpolator(d3.interpolateViridis);
    }

    public plot_vertices(ddGraph: DDGraph, groupVerticesIS: any, groupVerticesFS: any, groupVerticesTrace: any)  {

        //////////////////////////////
        // Itemsets
        //////////////////////////////
        //let joinVerticesIS = groupVerticesIS.selectAll("g")
        //    .data(ddGraph.verticesHierSet, (v: DDGVertexHSet) => v.id);
        
        let joinVerticesIS = groupVerticesIS.selectAll("g")
            .data(ddGraph.verticesHierSet, (v: DDGVertexHSet) => v.id)
            .join("g")
            .attr('transform', (v:DDGVertexHSet) => `translate(${v.x}, ${v.y})`);
        this.plot_itemset_vertex(joinVerticesIS);

        //////////////////////////////
        // Flowsplits
        //////////////////////////////
        let joinVerticesFS = groupVerticesFS.selectAll("g")
            .data(ddGraph.verticesFlowSplit, (v: DDGVertexFlowSplit) => v.id)
            .join("g")
            .attr("class", "vertex")
            .attr('transform', (v: DDGVertexFlowSplit) => `translate(${v.x}, ${v.y})`);
        
        this.plot_flow_splits(joinVerticesFS);

        //////////////////////////////
        // Trace
        //////////////////////////////
        let joinVerticesTrace = groupVerticesTrace.selectAll("g")
            .data(
                ddGraph.verticesEMDLeft.concat(ddGraph.verticesEMDRight).filter(v => v.show), 
                (v: DDGVertexEMDTrace) => v.id
            )
            .join("g");
        
        let groupsVertexTrace = joinVerticesTrace
            .attr("class", "vertex")
            .attr('transform', (v: DDGVertexEMDTrace) => `translate(${v.x}, ${v.y})`);
        this.plot_emd_vertex(groupsVertexTrace);

    }

    public plot_edges(ddGraph: DDGraph, groupEdgesIS: any, groupEdgesFS: any, groupEdgesEMD: any) {

        const link = d3.linkHorizontal();

        //////////////////////////////
        // Itemsets
        //////////////////////////////
        let joinEdgesIS = groupEdgesIS.selectAll("path")
            .data(ddGraph.edges.INTERSET, (e: DDGEdgeInterset) => e.id)
            .join("path")
                .attr("class", "edge")
                .attr("class", "edgeIS")
                .attr("d", link);

        //////////////////////////////
        // Flowsplits
        //////////////////////////////
        let joinEdgesFS = groupEdgesFS.selectAll("path")
            .data(ddGraph.edges.FLOWSPLIT.filter(e => e.visible && e.show), (e: DDGEdgeFlowsplit) => e.id)
            .join("path")
                .attr("class", "edge")
                .attr("class", "edgeFS")
                .attr("d", (e: DDGEdgeFlowsplit) => `M${e.ux} ${e.uy} L${e.vx} ${e.vy} `)
                //.attr("d", link)
                .attr("stroke-width", (e: DDGEdgeFlowsplit) => e.width);

        //////////////////////////////
        // EMD
        //////////////////////////////
        let joinEdgesEMD = groupEdgesEMD.selectAll("path")
            .data(ddGraph.edges.EMDFLOW.filter(e => e.show), (e: DDGEdgeEMD) => e.id)
            .join("path")
                .attr("class", "edge")
                .attr("class", "edgeEMD")
                .attr("d", link)
                .attr("stroke", (e: DDGEdgeEMD) => this.costContrColorMap(e.lpCostContrib))
                .attr("stroke-width", (e: DDGEdgeEMD) => e.width);
    } 

    private plot_flow_splits(groupsVertexFS: d3.Selection<SVGGElement, DDGVertexFlowSplit, any, any>) {
        groupsVertexFS
            .append("rect")
                .attr("class", "vertexFSBoundary")
                .attr("x", 0)
                .attr("y", 0)
                .attr("width", (v : DDGVertex) => v.size.width)
                .attr("height", (v : DDGVertex) => v.size.height)
                .attr("fill", "blue");

        groupsVertexFS
            .append("g")
            .attr('transform', v => 
                `translate(${v.probPicto.x + ((v.probPicto.size.width) / 2)}, ${v.probPicto.y + v.probPicto.size.height / 2})`)
            .selectAll("g")
            .data(v => [v.probPicto.probLeftLog, v.probPicto.probRightLog])
            .join(
                enter => enter
                    .append("g")
                    .each(function(pictoProb: DDGLogProbDonut) {
                        let arcGen = d3.arc();
                        let sel: d3.Selection<SVGGElement, DDGLogProbDonut, null, undefined>  = d3.select(this);
                        sel
                            .selectAll('path')
                            .data(( pP: DDGLogProbDonut)  => pP.entries)
                            .join('path')
                                .attr('class', dEntry => `probDonut prob${dEntry.type}`)
                                .attr('d', dEntry => arcGen({innerRadius: pictoProb.innerR, outerRadius: pictoProb.outerR, startAngle: dEntry.startAngle, endAngle: dEntry.endAngle}));

                    }),
                update => update,
                exit => exit.remove());
    }

    private plot_emd_vertex(vertexGroupEMD: d3.Selection<SVGGElement, DDGVertexEMDTrace, any, any>) {
        const groupActivity = vertexGroupEMD
            .selectAll("g")
            .data(v => v.activities)
                .join("g")
                .attr('transform', a => `translate(${a.x}, ${a.y})`);

        groupActivity.append("path") 
                    .attr("class", "vertexEMDActivity")
                    .attr("d", a => a.getPath())
                    .attr("fill", a => {
                        if (a.activityCode !== undefined) {
                            return this.actColorMap(a.activityCode.toString());
                        }
                        else {
                            return "gray";
                        }});
        groupActivity
            .append("text")
                .attr("class", "vertexEMDActivityLabel")
                .attr("x", a => a.textLabel.x + a.textLabel.posTextAnchor.x)
                .attr("y", a => a.textLabel.y + a.textLabel.posTextAnchor.y)
                .style("font-size", a => `${a.textLabel.fontSize}px`)
                .style("font-family", a=> `${a.textLabel.font}`)
                .text(a => a.textLabel.text);
    }

    private plot_itemset_vertex(vertexGroupEMD: d3.Selection<SVGGElement, DDGVertexHSet, any, any>): void {
        ////////////////////////////////////////
        // Frame
        ////////////////////////////////////////
        // Border
        vertexGroupEMD.append("rect")
            .attr("class", "vertexISBoundary")
            .attr("width", v => v.size.width)
            .attr("height", v => v.size.height)
            .attr("fill", "none");

        // Activity Row - Probability Pictogram Separator
        vertexGroupEMD.append("path")
            .attr("class", "vertexISBoundaryInner")
            .attr("d", v => `M 0 
                ${v.innerVertexSep + v.actSetOverviewTitle.size.height + Math.max(v.actSetOverview.size.height, v.condActSetOverview?.size.height ?? 0) + 2 * v.childSep} 
                h ${v.size.width}`);

            
        ////////////////////////////////////////
        // Activity Set Overview
        ////////////////////////////////////////
        // Title
        vertexGroupEMD.append('text')
            .attr("x", v => v.actSetOverviewTitle.x + v.actSetOverviewTitle.posTextAnchor.x)
            .attr("y", v => v.actSetOverviewTitle.y + v.actSetOverviewTitle.posTextAnchor.y)
            .attr('class', "actSetOverviewTitle")
            .style("font-size", v => `${v.actSetOverviewTitle.fontSize}px`)
            .style("font-family", v => `${v.actSetOverviewTitle.font}`)
            .text(v => v.actSetOverviewTitle.text);
        // Append activity row group
        const vGroupISAct = vertexGroupEMD
            .append("g")
            .attr('transform', v => `translate(${v.actSetOverview.x}, ${v.actSetOverview.y})`);
        this.plot_itemset_overview(vGroupISAct);
        // Append conditional activity row group
        const vGroupISCondAct = vertexGroupEMD
            .append("g")
            .attr('transform', v => `translate(${v.condActSetOverview?.x ?? 0}, ${v.condActSetOverview?.y ?? 0})`);
        this.plot_conditional_itemset_overview(vGroupISCondAct);
        
        ////////////////////////////////////////
        // Activity Set Probability Pictograms
        ////////////////////////////////////////
        // title
        vertexGroupEMD.append('text')
            .attr("x", v => v.probPictoTitle.x + v.probPictoTitle.posTextAnchor.x)
            .attr("y", v => v.probPictoTitle.y + v.probPictoTitle.posTextAnchor.y)
            .attr('class', "probDonutTitle")
            .style("font-size", v => `${v.probPictoTitle.fontSize}px`)
            .style("font-family", v => `${v.probPictoTitle.font}`)
            .text(v => v.probPictoTitle.text);

        // Group Picto
        const vGroupISProb = vertexGroupEMD
            .append("g")
            .attr('transform', v => 
                `translate(${v.probPicto.x + ((v.probPicto.size.width) / 2)}, ${v.probPicto.y + v.probPicto.size.height / 2})`);

        vGroupISProb
            .selectAll("g")
            .data(v => [v.probPicto.probLeftLog, v.probPicto.probRightLog])
            .join(
                enter => enter
                    .append("g")
                    .each(function(pictoProb: DDGLogProbDonut) {
                        let arcGen = d3.arc();
                        let sel: d3.Selection<SVGGElement, DDGLogProbDonut, null, undefined>  = d3.select(this);
                        sel
                            .selectAll('path')
                            .data(( pP: DDGLogProbDonut)  => pP.entries)
                            .join('path')
                                .attr('class', dEntry => `probDonut prob${dEntry.type}`)
                                .attr('d', dEntry => arcGen({innerRadius: pictoProb.innerR, outerRadius: pictoProb.outerR, startAngle: dEntry.startAngle, endAngle: dEntry.endAngle}));

                    }),
                update => update,
                exit => exit.remove()
            );

        ////////////////////
        // Conditional Pictogram
        ////////////////////
        // Group Picto
        const vGroupISProbCond = vertexGroupEMD
            .append("g")
            .attr('transform', v => 
                `translate(${((v.probCondPicto?.x ?? 0) + ((v.probCondPicto?.size.width ?? 0) / 2))}, ${(v.probCondPicto?.y ?? 0) + (v.probCondPicto?.size.height ?? 0) / 2})`);

        vGroupISProbCond
            .selectAll("g")
            .data(v => (v.probCondPicto != undefined ? [v.probCondPicto.probLeftLog, v.probCondPicto.probRightLog] : []))
            .join(
                enter => enter
                    .append("g")
                    .each(function(pictoProb: DDGLogProbDonut) {
                        let arcGen = d3.arc();
                        let sel: d3.Selection<SVGGElement, DDGLogProbDonut, null, undefined>  = d3.select(this);
                        sel
                            .selectAll('path')
                            .data(( pP: DDGLogProbDonut)  => pP.entries)
                            .join('path')
                                .attr('class', dEntry => `probDonut prob${dEntry.type}`)
                                .attr('d', dEntry => arcGen({innerRadius: pictoProb.innerR, outerRadius: pictoProb.outerR, startAngle: dEntry.startAngle, endAngle: dEntry.endAngle}));

                    }),
                update => update,
                exit => exit.remove()
            );
    }

    private plot_itemset_overview(groupActSet: d3.Selection<SVGGElement, DDGVertexHSet, any, any>) {
        // Nested Join activity set array
        const groupActivity = groupActSet.selectAll("g")
            .data(v => v.actSetOverview.activitySet)
            .join("g")
                .attr('transform', a => `translate(${a.x}, ${a.y})`);

        groupActivity     
            .append("path")
                .attr("class", "vertexISActivity")
                .attr("d", a => a.getPath())
                .attr("fill", a => {
                    if (a.activityCode !== undefined) {
                        return this.actColorMap(a.activityCode.toString());
                    }
                    else {
                        return "gray";
                    }});

        groupActivity
            .append("text")
                .attr("class", "vertexSetActivityLabel")
                .attr("x", a => a.textLabel.x + a.textLabel.posTextAnchor.x)
                .attr("y", a => a.textLabel.y + a.textLabel.posTextAnchor.y)
                .style("font-size", a => `${a.textLabel.fontSize}px`)
                .style("font-family", a => `${a.textLabel.font}`)
                .text(a => a.textLabel.text);
    }

    private plot_conditional_itemset_overview(groupCondActSet: d3.Selection<SVGGElement, DDGVertexHSet, any, any>) {
        // Nested Join activity set array
        const groupActivity = groupCondActSet.selectAll("g")
            .data(v => v.condActSetOverview?.activitySet ?? [])
            .join("g")
                .attr('transform', a => `translate(${a.x}, ${a.y})`);

        groupActivity     
            .append("path")
                .attr("class", "vertexISCondActivity")
                .attr("d", a => a.getPath())
                .attr("fill", a => {
                    if (a.activityCode !== undefined) {
                        return this.actColorMap(a.activityCode.toString());
                    }
                    else {
                        return "gray";
                    }});

        groupActivity
            .append("text")
                .attr("class", "vertexSetCondActivityLabel")
                .attr("x", a => a.textLabel.x + a.textLabel.posTextAnchor.x)
                .attr("y", a => a.textLabel.y + a.textLabel.posTextAnchor.y)
                .style("font-size", a => `${a.textLabel.fontSize}px`)
                .style("font-family", a => `${a.textLabel.font}`)
                .text(a => a.textLabel.text);
    }
}
