import { DDGActivity } from "./DDGActivity";
import { DDGActivityTrace } from "./DDGActivityTrace";
import { DDGBuilder } from "../DDGBuilder";
import { TextSizeComputer } from "../util/TextSizeComputer";
import { DDGTextLabelEl } from "../auxelements/DDGTextLabelEl";
import { DDGActStateData } from "./DDGActivityTextMeasures";

export class DDGActivityTraceSink extends DDGActivityTrace {

	public static readonly SYMBOL = '\u27C2';

    public constructor(textLabels: DDGActStateData<DDGTextLabelEl>) {
        super(DDGActivityTraceSink.SYMBOL , -1, textLabels, undefined);
	}

	public override getPath(): string {
		return `M ${this.x} ${this.y} L ${this.x + this.size.width} ${this.y} 
			L ${this.x + this.size.width} ${this.y + this.size.height} 
			L ${this.x} ${this.y + this.size.height} Z `;
    }


}