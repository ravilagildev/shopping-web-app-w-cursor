export type RoastLevel = 'LIGHT' | 'MEDIUM' | 'MEDIUM_DARK' | 'DARK';

export interface Roaster {
  id: number;
  name: string;
  location?: string;
  website?: string;
  notes?: string;
  coffees: Coffee[];
  totalSpent: number;
  coffeeCount: number;
}

export interface Coffee {
  id: number;
  coffeeName: string;
  roastDate: string; // ISO date string
  purchaseDate: string; // ISO date string
  initialWeight: number; // grams
  currentWeight: number; // grams
  origin?: string;
  roastLevel?: RoastLevel;
  processingMethod?: string;
  price?: number;
  notes?: string;
  roasterId: number;
  roasterName: string;
  daysSinceRoast?: number;
  percentageRemaining?: number;
}

export interface InventorySummary {
  totalWeight: number; // grams
  totalBags: number;
  averagePricePerGram: number;
  totalSpent: number;
  lowStockCoffees: Coffee[]; // < 20% remaining
  agingCoffees: Coffee[]; // > 30 days since roast
  roasters: Roaster[];
}
