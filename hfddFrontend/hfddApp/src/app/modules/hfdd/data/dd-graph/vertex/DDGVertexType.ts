export enum DDGVertexType {
    INTERSET = 'INTERSET',
    INTERSETCS = 'INTERSETCS',
    FLOWSPLIT = 'FLOWSPLIT',
    EMDLEFT = 'EMDLEFT',
    EMDRIGHT = 'EMDRIGHT',
    ARTROOT = 'ARTROOT'
}
 
//export type DDGVertexTypeMap<R> =  { [key in keyof typeof DDGVertexType]: R};
export type DDGVertexTypeMap<R> =  Record<DDGVertexType, R>;
