export enum DDGActivityTextState {
    NONE = 'NONE',
    SUPERSHORT = 'SUPERSHORT',
    SHORT = 'SHORT',
    NORMAL = 'NORMAL'
}

export type DDGActStateData<R> = Record<DDGActivityTextState, R>;