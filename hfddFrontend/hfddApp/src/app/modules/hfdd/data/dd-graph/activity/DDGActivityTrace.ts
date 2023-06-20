import { DDGActivity } from "./DDGActivity";

export class DDGActivityTrace extends DDGActivity {
	private _chevronInCut = 5;

	public override calcMinSize(): void {
		super.calcMinSize();
		// Add incuts
		this.size.widthMin += 2 * this._chevronInCut;
	}

	public getPath(): string {
		return `M 0 0 L ${this.size.width - this._chevronInCut} 0
			L ${this.size.width} ${this.size.height / 2} 
			L ${this.size.width - this._chevronInCut} ${this.size.height}
			L 0 ${this.y + this.size.height} L ${this._chevronInCut} ${this.size.height / 2} Z `
	}

	//public getPath(): string {
	//	return `M ${this.x} ${this.y} L ${this.x + this.size.width - this._chevronInCut} ${this.y} 
	//		L ${this.x + this.size.width} ${this.y + this.size.height / 2} 
	//		L ${this.x + this.size.width - this._chevronInCut} ${this.y + this.size.height}
	//		L ${this.x} ${this.y + this.size.height} L ${this.x + this._chevronInCut} ${this.y + this.size.height / 2} Z `
	//}

}