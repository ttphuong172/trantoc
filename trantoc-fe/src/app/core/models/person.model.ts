export interface Person {
  id: number;

  fullName: string;

  aliasName?: string;

  birthYear?: number;

  deathYear?: number;

  gender: string;

  rootAncestor: boolean;

  notes?: string;
}
