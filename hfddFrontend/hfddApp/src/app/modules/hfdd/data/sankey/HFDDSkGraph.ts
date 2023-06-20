import { HFDDSkEdge } from "./HFDDSkEdge";
import { HFDDSkVertex } from "./HFDDSkVertex";

export interface HFDDSkGraph {
    Edges: HFDDSkEdge[];
    Vertices: HFDDSkVertex[];
}