import { DDGVertexHSet } from "./DDGVertexHSet";
/*
* Vertex Layout:
*    ---------------------------
*    |SEP
*    |svgGroup: Rows of activities
*    |  Symbol for empty set
*    |2*SEP (child)
*    |svgGroup: Probability pictograms | SEP
*    |SEP
*    ---------------------------
*/
export class DDGVertexArtRoot extends DDGVertexHSet {

}