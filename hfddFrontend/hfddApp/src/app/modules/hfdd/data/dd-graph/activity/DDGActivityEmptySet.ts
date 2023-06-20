import { DDGActivity } from "./DDGActivity";
import { TextSizeComputer } from "../util/TextSizeComputer";
import { DDGTextLabelEl } from "../auxelements/DDGTextLabelEl";
import { DDGActStateData } from "./DDGActivityTextMeasures";

export class DDGActivityEmptySet extends DDGActivity {

	public static readonly SYMBOL = '\u2205';

    public constructor(textLabels: DDGActStateData<DDGTextLabelEl>) {
        super(DDGActivityEmptySet.SYMBOL, -1, textLabels, undefined);
	}

	public override getPath(): string {
		return `M ${this.x} ${this.y} L ${this.x + this.size.width} ${this.y} 
			L ${this.x + this.size.width} ${this.y + this.size.height} 
			L ${this.x} ${this.y + this.size.height} Z `;
    }


}