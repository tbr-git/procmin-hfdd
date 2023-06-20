import { DDGActivity } from "./DDGActivity";

export class DDGActivitySet extends DDGActivity {

	public getPath(): string {
		return `M 0 0 L ${this.size.width} 0
			L ${this.size.width} ${this.size.height} 
			L 0 ${this.size.height} Z`
	}

}