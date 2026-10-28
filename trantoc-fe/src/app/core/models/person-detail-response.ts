export interface PersonDetailResponse {
  id: number;

  fullName: string;

  aliasName?: string;

  generation?: number;

  birthYear?: number;

  deathYear?: number;

  notes?: string;

  gender: string;

  address?: string;

  burialPlace: string;

  deathAnniversary: string;

  rootAncestor: boolean;

  role: string;
}
