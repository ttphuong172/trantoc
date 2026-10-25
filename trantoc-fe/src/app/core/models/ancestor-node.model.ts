export interface AncestorNode {
  id: number;

  fullName: string;

  aliasName?: string;

  gender: string;

  birthYear?: number;

  deathYear?: number;

  parents: AncestorNode[];
}
