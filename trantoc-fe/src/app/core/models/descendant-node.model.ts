export interface DescendantNode {
  id: number;

  fullName: string;

  aliasName?: string;

  children: DescendantNode[];
}
