import { useState, useEffect } from 'react';
import { Roaster, Coffee, InventorySummary, RoastLevel } from './types';
import { roasterApi, coffeeApi, inventoryApi, authApi } from './api';
import Login from './Login';
import './App.css';

function App() {
  const [roasters, setRoasters] = useState<Roaster[]>([]);
  const [coffees, setCoffees] = useState<Coffee[]>([]);
  const [inventorySummary, setInventorySummary] = useState<InventorySummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [authChecking, setAuthChecking] = useState(true);
  const [showRoasterForm, setShowRoasterForm] = useState(false);
  const [showCoffeeForm, setShowCoffeeForm] = useState(false);
  const [editingRoaster, setEditingRoaster] = useState<Roaster | null>(null);
  const [editingCoffee, setEditingCoffee] = useState<Coffee | null>(null);
  const [selectedRoasterId, setSelectedRoasterId] = useState<number | null>(null);

  useEffect(() => {
    const authed = authApi.isAuthenticated();
    setIsAuthenticated(authed);
    setAuthChecking(false);

    if (authed) {
      loadData();
    } else {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      loadData();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  const loadData = async () => {
    if (!isAuthenticated) return;
    try {
      setLoading(true);
      const [roastersRes, coffeesRes, inventoryRes] = await Promise.all([
        roasterApi.getAll(),
        coffeeApi.getAll(),
        inventoryApi.getSummary(),
      ]);
      setRoasters(roastersRes.data);
      setCoffees(coffeesRes.data);
      setInventorySummary(inventoryRes.data);
    } catch (error) {
      console.error('Error loading data:', error);
      alert('Error loading data. Make sure the backend is running on port 8080.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRoaster = async (name: string, location?: string, website?: string, notes?: string) => {
    try {
      await roasterApi.create({ name, location, website, notes });
      await loadData();
      setShowRoasterForm(false);
    } catch (error) {
      console.error('Error creating roaster:', error);
      alert('Error creating roaster');
    }
  };

  const handleUpdateRoaster = async (id: number, name: string, location?: string, website?: string, notes?: string) => {
    try {
      await roasterApi.update(id, { name, location, website, notes });
      await loadData();
      setEditingRoaster(null);
    } catch (error) {
      console.error('Error updating roaster:', error);
      alert('Error updating roaster');
    }
  };

  const handleDeleteRoaster = async (id: number) => {
    if (!confirm('Are you sure you want to delete this roaster and all their coffees?')) {
      return;
    }
    try {
      await roasterApi.delete(id);
      await loadData();
    } catch (error) {
      console.error('Error deleting roaster:', error);
      alert('Error deleting roaster');
    }
  };

  const handleCreateCoffee = async (coffeeData: {
    coffeeName: string;
    roastDate: string;
    purchaseDate: string;
    initialWeight: number;
    currentWeight?: number;
    origin?: string;
    roastLevel?: RoastLevel;
    processingMethod?: string;
    price?: number;
    notes?: string;
    roasterId: number;
  }) => {
    try {
      await coffeeApi.create(coffeeData);
      await loadData();
      setShowCoffeeForm(false);
      setSelectedRoasterId(null);
    } catch (error) {
      console.error('Error creating coffee:', error);
      alert('Error creating coffee');
    }
  };

  const handleUpdateCoffee = async (id: number, coffeeData: {
    coffeeName: string;
    roastDate: string;
    purchaseDate: string;
    initialWeight: number;
    currentWeight: number;
    origin?: string;
    roastLevel?: RoastLevel;
    processingMethod?: string;
    price?: number;
    notes?: string;
    roasterId: number;
  }) => {
    try {
      await coffeeApi.update(id, coffeeData);
      await loadData();
      setEditingCoffee(null);
    } catch (error) {
      console.error('Error updating coffee:', error);
      alert('Error updating coffee');
    }
  };

  const handleConsumeCoffee = async (id: number, amount: number) => {
    try {
      await coffeeApi.consume(id, amount);
      await loadData();
    } catch (error) {
      console.error('Error consuming coffee:', error);
      alert('Error consuming coffee');
    }
  };

  const handleDeleteCoffee = async (id: number) => {
    if (!confirm('Are you sure you want to delete this coffee?')) {
      return;
    }
    try {
      await coffeeApi.delete(id);
      await loadData();
    } catch (error) {
      console.error('Error deleting coffee:', error);
      alert('Error deleting coffee');
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const formatWeight = (grams: number) => {
    if (grams >= 1000) {
      return `${(grams / 1000).toFixed(2)} kg`;
    }
    return `${grams.toFixed(0)} g`;
  };

  const getFreshnessColor = (daysSinceRoast?: number) => {
    if (!daysSinceRoast) return '';
    if (daysSinceRoast <= 14) return 'fresh';
    if (daysSinceRoast <= 30) return 'aging';
    return 'stale';
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  if (authChecking) {
    return <div className="loading">Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Login onLogin={() => {
      setIsAuthenticated(true);
      loadData();
    }} />;
  }

  return (
    <div className="app">
      <header className="header">
        <h1>☕ Specialty Coffee Tracker</h1>
        <div className="header-actions">
          <button
            onClick={() => {
              authApi.logout();
              setIsAuthenticated(false);
              setRoasters([]);
              setCoffees([]);
              setInventorySummary(null);
            }}
            className="btn btn-secondary"
          >
            Logout
          </button>
        </div>
      </header>

      {inventorySummary && (
        <div className="budget-summary">
          <div className="budget-card">
            <h3>Total Inventory</h3>
            <p className="amount">{formatWeight(inventorySummary.totalWeight)}</p>
          </div>
          <div className="budget-card">
            <h3>Total Bags</h3>
            <p className="amount">{inventorySummary.totalBags}</p>
          </div>
          <div className="budget-card">
            <h3>Total Spent</h3>
            <p className="amount">{formatCurrency(inventorySummary.totalSpent)}</p>
          </div>
          <div className="budget-card">
            <h3>Avg Price/Gram</h3>
            <p className="amount">{formatCurrency(inventorySummary.averagePricePerGram)}</p>
          </div>
        </div>
      )}

      {inventorySummary && (inventorySummary.lowStockCoffees.length > 0 || inventorySummary.agingCoffees.length > 0) && (
        <div className="alerts">
          {inventorySummary.lowStockCoffees.length > 0 && (
            <div className="alert alert-warning">
              <h3>⚠️ Low Stock ({inventorySummary.lowStockCoffees.length})</h3>
              <ul>
                {inventorySummary.lowStockCoffees.map(coffee => (
                  <li key={coffee.id}>
                    {coffee.coffeeName} - {formatWeight(coffee.currentWeight)} remaining
                    ({coffee.percentageRemaining?.toFixed(0)}%)
                  </li>
                ))}
              </ul>
            </div>
          )}
          {inventorySummary.agingCoffees.length > 0 && (
            <div className="alert alert-info">
              <h3>⏰ Aging Coffee ({inventorySummary.agingCoffees.length})</h3>
              <ul>
                {inventorySummary.agingCoffees.map(coffee => (
                  <li key={coffee.id}>
                    {coffee.coffeeName} - {coffee.daysSinceRoast} days since roast
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      <div className="actions">
        <button onClick={() => setShowRoasterForm(true)} className="btn btn-primary">
          + Add Roaster
        </button>
        <button onClick={() => setShowCoffeeForm(true)} className="btn btn-primary">
          + Add Coffee
        </button>
      </div>

      {showRoasterForm && (
        <RoasterForm
          onSubmit={handleCreateRoaster}
          onCancel={() => setShowRoasterForm(false)}
        />
      )}

      {editingRoaster && (
        <RoasterForm
          roaster={editingRoaster}
          onSubmit={(name, location, website, notes) => handleUpdateRoaster(editingRoaster.id, name, location, website, notes)}
          onCancel={() => setEditingRoaster(null)}
        />
      )}

      {showCoffeeForm && (
        <CoffeeForm
          roasters={roasters}
          selectedRoasterId={selectedRoasterId}
          onSubmit={handleCreateCoffee}
          onCancel={() => {
            setShowCoffeeForm(false);
            setSelectedRoasterId(null);
          }}
        />
      )}

      {editingCoffee && (
        <CoffeeForm
          coffee={editingCoffee}
          roasters={roasters}
          onSubmit={(data) => handleUpdateCoffee(editingCoffee.id, data)}
          onCancel={() => setEditingCoffee(null)}
        />
      )}

      <div className="persons-grid">
        {roasters.map((roaster) => {
          const roasterCoffees = coffees.filter((c) => c.roasterId === roaster.id);
          return (
            <div key={roaster.id} className="person-card">
              <div className="person-header">
                <h2>{roaster.name}</h2>
                {roaster.location && <p className="roaster-location">{roaster.location}</p>}
                <div className="person-actions">
                  <button
                    onClick={() => {
                      setEditingRoaster(roaster);
                      setShowRoasterForm(false);
                    }}
                    className="btn btn-small"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => handleDeleteRoaster(roaster.id)}
                    className="btn btn-small btn-danger"
                  >
                    Delete
                  </button>
                </div>
              </div>
              <div className="person-total">
                <div>Total Spent: <strong>{formatCurrency(roaster.totalSpent)}</strong></div>
                <div>Coffees: <strong>{roaster.coffeeCount}</strong></div>
              </div>
              <div className="gifts-list">
                <h3>Coffees:</h3>
                {roasterCoffees.length === 0 ? (
                  <p className="no-gifts">No coffees yet</p>
                ) : (
                  <ul>
                    {roasterCoffees.map((coffee) => (
                      <li key={coffee.id} className={`gift-item ${getFreshnessColor(coffee.daysSinceRoast)}`}>
                        <div className="gift-info">
                          <span className="gift-description">{coffee.coffeeName}</span>
                          {coffee.origin && <span className="coffee-origin">{coffee.origin}</span>}
                          <span className="coffee-details">
                            {coffee.roastLevel && <span className="roast-level">{coffee.roastLevel}</span>}
                            {coffee.daysSinceRoast !== undefined && (
                              <span className={`freshness ${getFreshnessColor(coffee.daysSinceRoast)}`}>
                                {coffee.daysSinceRoast} days old
                              </span>
                            )}
                          </span>
                        </div>
                        <div className="gift-meta">
                          <span className="gift-price">{formatWeight(coffee.currentWeight)}</span>
                          {coffee.price && <span className="coffee-price">{formatCurrency(coffee.price)}</span>}
                        </div>
                        <div className="gift-actions">
                          <button
                            onClick={() => {
                              const amount = prompt('How many grams to consume?', '20');
                              if (amount) {
                                handleConsumeCoffee(coffee.id, parseFloat(amount));
                              }
                            }}
                            className="btn btn-tiny btn-secondary"
                          >
                            Consume
                          </button>
                          <button
                            onClick={() => {
                              setEditingCoffee(coffee);
                              setShowCoffeeForm(false);
                            }}
                            className="btn btn-tiny"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDeleteCoffee(coffee.id)}
                            className="btn btn-tiny btn-danger"
                          >
                            Delete
                          </button>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
                <button
                  onClick={() => {
                    setSelectedRoasterId(roaster.id);
                    setShowCoffeeForm(true);
                  }}
                  className="btn btn-small btn-secondary"
                >
                  + Add Coffee
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {roasters.length === 0 && (
        <div className="empty-state">
          <p>No roasters added yet. Start by adding a roaster!</p>
        </div>
      )}
    </div>
  );
}

interface RoasterFormProps {
  roaster?: Roaster;
  onSubmit: (name: string, location?: string, website?: string, notes?: string) => void;
  onCancel: () => void;
}

function RoasterForm({ roaster, onSubmit, onCancel }: RoasterFormProps) {
  const [name, setName] = useState(roaster?.name || '');
  const [location, setLocation] = useState(roaster?.location || '');
  const [website, setWebsite] = useState(roaster?.website || '');
  const [notes, setNotes] = useState(roaster?.notes || '');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (name.trim()) {
      onSubmit(name.trim(), location.trim() || undefined, website.trim() || undefined, notes.trim() || undefined);
      setName('');
      setLocation('');
      setWebsite('');
      setNotes('');
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2>{roaster ? 'Edit Roaster' : 'Add Roaster'}</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Name:</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              autoFocus
            />
          </div>
          <div className="form-group">
            <label>Location (optional):</label>
            <input
              type="text"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>Website (optional):</label>
            <input
              type="url"
              value={website}
              onChange={(e) => setWebsite(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>Notes (optional):</label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={3}
            />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {roaster ? 'Update' : 'Add'}
            </button>
            <button type="button" onClick={onCancel} className="btn btn-secondary">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

interface CoffeeFormProps {
  coffee?: Coffee;
  roasters: Roaster[];
  selectedRoasterId?: number | null;
  onSubmit: (data: {
    coffeeName: string;
    roastDate: string;
    purchaseDate: string;
    initialWeight: number;
    currentWeight?: number;
    origin?: string;
    roastLevel?: RoastLevel;
    processingMethod?: string;
    price?: number;
    notes?: string;
    roasterId: number;
  }) => void;
  onCancel: () => void;
}

function CoffeeForm({ coffee, roasters, selectedRoasterId, onSubmit, onCancel }: CoffeeFormProps) {
  const [coffeeName, setCoffeeName] = useState(coffee?.coffeeName || '');
  const [roastDate, setRoastDate] = useState(coffee?.roastDate || new Date().toISOString().split('T')[0]);
  const [purchaseDate, setPurchaseDate] = useState(coffee?.purchaseDate || new Date().toISOString().split('T')[0]);
  const [initialWeight, setInitialWeight] = useState(coffee?.initialWeight.toString() || '');
  const [currentWeight, setCurrentWeight] = useState(coffee?.currentWeight?.toString() || '');
  const [origin, setOrigin] = useState(coffee?.origin || '');
  const [roastLevel, setRoastLevel] = useState<RoastLevel | ''>(coffee?.roastLevel || '');
  const [processingMethod, setProcessingMethod] = useState(coffee?.processingMethod || '');
  const [price, setPrice] = useState(coffee?.price?.toString() || '');
  const [notes, setNotes] = useState(coffee?.notes || '');
  const [roasterId, setRoasterId] = useState<number>(
    coffee?.roasterId || selectedRoasterId || roasters[0]?.id || 0
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (coffeeName.trim() && roastDate && purchaseDate && initialWeight && roasterId) {
      onSubmit({
        coffeeName: coffeeName.trim(),
        roastDate,
        purchaseDate,
        initialWeight: parseFloat(initialWeight),
        currentWeight: currentWeight ? parseFloat(currentWeight) : parseFloat(initialWeight),
        origin: origin.trim() || undefined,
        roastLevel: roastLevel || undefined,
        processingMethod: processingMethod.trim() || undefined,
        price: price ? parseFloat(price) : undefined,
        notes: notes.trim() || undefined,
        roasterId,
      });
      // Reset form
      setCoffeeName('');
      setRoastDate(new Date().toISOString().split('T')[0]);
      setPurchaseDate(new Date().toISOString().split('T')[0]);
      setInitialWeight('');
      setCurrentWeight('');
      setOrigin('');
      setRoastLevel('');
      setProcessingMethod('');
      setPrice('');
      setNotes('');
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal" style={{ maxWidth: '600px' }}>
        <h2>{coffee ? 'Edit Coffee' : 'Add Coffee'}</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Roaster:</label>
            <select
              value={roasterId}
              onChange={(e) => setRoasterId(Number(e.target.value))}
              required
            >
              {roasters.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.name}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Coffee Name:</label>
            <input
              type="text"
              value={coffeeName}
              onChange={(e) => setCoffeeName(e.target.value)}
              required
              autoFocus
              placeholder="e.g., Ethiopian Yirgacheffe"
            />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Roast Date:</label>
              <input
                type="date"
                value={roastDate}
                onChange={(e) => setRoastDate(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label>Purchase Date:</label>
              <input
                type="date"
                value={purchaseDate}
                onChange={(e) => setPurchaseDate(e.target.value)}
                required
              />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Initial Weight (grams):</label>
              <input
                type="number"
                value={initialWeight}
                onChange={(e) => setInitialWeight(e.target.value)}
                min="0"
                step="0.1"
                required
              />
            </div>
            <div className="form-group">
              <label>Current Weight (grams):</label>
              <input
                type="number"
                value={currentWeight}
                onChange={(e) => setCurrentWeight(e.target.value)}
                min="0"
                step="0.1"
                placeholder={initialWeight || 'Auto'}
              />
            </div>
          </div>
          <div className="form-group">
            <label>Origin (optional):</label>
            <input
              type="text"
              value={origin}
              onChange={(e) => setOrigin(e.target.value)}
              placeholder="e.g., Ethiopia, Yirgacheffe"
            />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Roast Level (optional):</label>
              <select
                value={roastLevel}
                onChange={(e) => setRoastLevel(e.target.value as RoastLevel | '')}
              >
                <option value="">Select...</option>
                <option value="LIGHT">Light</option>
                <option value="MEDIUM">Medium</option>
                <option value="MEDIUM_DARK">Medium Dark</option>
                <option value="DARK">Dark</option>
              </select>
            </div>
            <div className="form-group">
              <label>Processing Method (optional):</label>
              <input
                type="text"
                value={processingMethod}
                onChange={(e) => setProcessingMethod(e.target.value)}
                placeholder="e.g., Washed, Natural, Honey"
              />
            </div>
          </div>
          <div className="form-group">
            <label>Price (optional):</label>
            <input
              type="number"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              min="0"
              step="0.01"
            />
          </div>
          <div className="form-group">
            <label>Notes (optional):</label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={3}
              placeholder="Tasting notes, rating, etc."
            />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {coffee ? 'Update' : 'Add'}
            </button>
            <button type="button" onClick={onCancel} className="btn btn-secondary">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default App;
