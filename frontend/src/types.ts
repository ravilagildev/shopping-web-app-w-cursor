export interface Person {
  id: number;
  name: string;
  gifts: Gift[];
  totalSpent: number;
}

export interface Gift {
  id: number;
  description: string;
  price: number;
  personId: number;
  personName: string;
}

export interface BudgetSummary {
  totalBudget: number;
  totalSpent: number;
  remaining: number;
  persons: Person[];
}

