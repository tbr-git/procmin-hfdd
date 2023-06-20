import * as d3Sankey from 'd3-sankey';
import { EdgeType } from './EdgeType';


export interface SNodeHFDDExtra {
    activities?: string[];
    nodeId: string;
    intraLevelKey: number;
    left: boolean; 
    probabilityMass: number; 
    residualProbabilityMass: number;
    skLevel: number; 
    type: string;
    variant?: number;
    matchingValue: number;
    matchingRelevant: boolean;
}

export interface SLinkHFDDExtra {
    cost: number;
    edgeType: EdgeType;
    edgeId: string;
    probabilityValue: number;
    matchingValue: number;
    matchingRelevant: boolean;
}

export type HFDDSNode = d3Sankey.SankeyNode<SNodeHFDDExtra, SLinkHFDDExtra>;
export type HFDDSLink = d3Sankey.SankeyLink<SNodeHFDDExtra, SLinkHFDDExtra>;

export interface HFDDFlowGraph {
    hfddNodes: HFDDSNode[];
    hfddLinks: HFDDSLink[];
}

export function hfdd_connect_sankey(vertexId: number|undefined, iteration: number|undefined, requestRes: any) {
    var hfddVertices : SNodeHFDDExtra[] = 
        requestRes.Vertices.map((v:any) => {

            var tmpActivities: string[]|undefined = undefined;
            var tmpVariant = undefined;
            var tmpMatchingValue = v.probabilityMass;
            if (v.type == 'itemset') {
                tmpActivities = v.activityItemset;
            }
            else if (v.type == 'trace') {
                tmpActivities = v.activityDescriptors;
                tmpVariant = v.variant;
                tmpMatchingValue = v.matchingWeight;
            }

            const hfddVertex : HFDDSNode = 
            {
                type: v.type,
                nodeId: `${vertexId || ''}_${v.id}`,
                activities: tmpActivities,
                intraLevelKey: v.intraLevelKey,
                left: v.left, 
                probabilityMass: v.probabilityMass, 
                residualProbabilityMass: v.residualProbabilityMass,
                skLevel: v.skLevel - 2, // 0-indexed after removing the artificial root
                variant: tmpVariant,
                matchingValue: tmpMatchingValue,
                matchingRelevant: v.matchingRelevant
            }
            return hfddVertex;
        });

    var idx = hfddVertices.findIndex(n => n.type == "root");
    hfddVertices.splice(idx, 1);

    const hfddEdges : HFDDSLink[] = 
        requestRes.Edges
        .filter((e:any) => e.Source != -1)
        .map((e:any) => {
            const link : HFDDSLink = 
            {
                edgeId: `${vertexId || ''}_${iteration || ''}_${e.Source}--${e.Target}`,
                source: `${vertexId || ''}_${e.Source}`,
                target: `${vertexId || ''}_${e.Target}`,
                value: e.EdgeInfo.flow,
                cost: e.EdgeInfo.cost,
                edgeType: e.EdgeInfo.EdgeType,
                probabilityValue: e.EdgeInfo.flow,
                matchingValue: e.EdgeInfo.matchingFlow,
                matchingRelevant: e.EdgeInfo.matchingRelevant
            }
            return link;
        })
    
    const connectedGraph: HFDDFlowGraph = {
        hfddNodes: hfddVertices,
        hfddLinks: hfddEdges
    }


    return connectedGraph;
}
