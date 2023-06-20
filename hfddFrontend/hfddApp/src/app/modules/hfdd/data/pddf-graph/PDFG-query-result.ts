import { PDFG } from "./PDFG";

export interface PDFGQueryResult {
    diffDFG: PDFG;
    dotString: string;
}