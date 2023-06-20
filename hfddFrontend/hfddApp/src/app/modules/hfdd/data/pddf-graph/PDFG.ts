import { PDFGEdge } from "./pdfg-edge";
import { PDFGVertex } from "./pdfg-vertex";

export interface PDFG {
    vertices: { [id: string]: PDFGVertex};
    edges: { [id: string]: PDFGEdge};
}