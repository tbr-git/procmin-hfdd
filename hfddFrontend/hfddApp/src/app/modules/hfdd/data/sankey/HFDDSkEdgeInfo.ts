import { EdgeType } from "./EdgeType";

export interface HFDDSkEdgeInfo {
    flow: number;
    cost: number;
    edgeType: EdgeType;
    matchingFlow: number;
    matchingRelevant: boolean;
}
