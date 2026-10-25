export interface PersonDetailResponse {
  id: number;

  fullName: string;

  aliasName?: string;

  generation?: number;

  birthYear?: number;

  deathYear?: number;

  notes?: string;

  gender: string;

  rootAncestor: boolean;

  role: string;
}
