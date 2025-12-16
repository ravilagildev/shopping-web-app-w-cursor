import { useState, useEffect } from 'react';
import { Person, Gift, BudgetSummary } from './types';
import { personApi, giftApi, budgetApi, authApi } from './api';
import Login from './Login';
import './App.css';

function App() {
  const [persons, setPersons] = useState<Person[]>([]);
  const [gifts, setGifts] = useState<Gift[]>([]);
  const [budgetSummary, setBudgetSummary] = useState<BudgetSummary | null>(null);
  const [totalBudget, setTotalBudget] = useState<number>(1000);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [authChecking, setAuthChecking] = useState(true);
  const [showPersonForm, setShowPersonForm] = useState(false);
  const [showGiftForm, setShowGiftForm] = useState(false);
  const [editingPerson, setEditingPerson] = useState<Person | null>(null);
  const [editingGift, setEditingGift] = useState<Gift | null>(null);
  const [selectedPersonId, setSelectedPersonId] = useState<number | null>(null);

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
  }, [isAuthenticated, totalBudget]);

  const loadData = async () => {
    if (!isAuthenticated) return;
    try {
      setLoading(true);
      const [personsRes, giftsRes, budgetRes] = await Promise.all([
        personApi.getAll(),
        giftApi.getAll(),
        budgetApi.getSummary(totalBudget),
      ]);
      setPersons(personsRes.data);
      setGifts(giftsRes.data);
      setBudgetSummary(budgetRes.data);
    } catch (error) {
      console.error('Error loading data:', error);
      alert('Error loading data. Make sure the backend is running on port 8080.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreatePerson = async (name: string) => {
    try {
      await personApi.create({ name });
      await loadData();
      setShowPersonForm(false);
    } catch (error) {
      console.error('Error creating person:', error);
      alert('Error creating person');
    }
  };

  const handleUpdatePerson = async (id: number, name: string) => {
    try {
      await personApi.update(id, { name });
      await loadData();
      setEditingPerson(null);
    } catch (error) {
      console.error('Error updating person:', error);
      alert('Error updating person');
    }
  };

  const handleDeletePerson = async (id: number) => {
    if (!confirm('Are you sure you want to delete this person and all their gifts?')) {
      return;
    }
    try {
      await personApi.delete(id);
      await loadData();
    } catch (error) {
      console.error('Error deleting person:', error);
      alert('Error deleting person');
    }
  };

  const handleCreateGift = async (description: string, price: number, personId: number) => {
    try {
      await giftApi.create({ description, price, personId });
      await loadData();
      setShowGiftForm(false);
      setSelectedPersonId(null);
    } catch (error) {
      console.error('Error creating gift:', error);
      alert('Error creating gift');
    }
  };

  const handleUpdateGift = async (id: number, description: string, price: number, personId: number) => {
    try {
      await giftApi.update(id, { description, price, personId });
      await loadData();
      setEditingGift(null);
    } catch (error) {
      console.error('Error updating gift:', error);
      alert('Error updating gift');
    }
  };

  const handleDeleteGift = async (id: number) => {
    if (!confirm('Are you sure you want to delete this gift?')) {
      return;
    }
    try {
      await giftApi.delete(id);
      await loadData();
    } catch (error) {
      console.error('Error deleting gift:', error);
      alert('Error deleting gift');
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
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
        <h1>🎄 Christmas Gifts Tracker</h1>
        <div className="header-actions">
          <div className="budget-input">
            <label>Total Budget: </label>
            <input
              type="number"
              value={totalBudget}
              onChange={(e) => setTotalBudget(Number(e.target.value))}
              min="0"
              step="0.01"
            />
          </div>
          <button
            onClick={() => {
              authApi.logout();
              setIsAuthenticated(false);
              setPersons([]);
              setGifts([]);
              setBudgetSummary(null);
            }}
            className="btn btn-secondary"
          >
            Logout
          </button>
        </div>
      </header>

      {budgetSummary && (
        <div className="budget-summary">
          <div className="budget-card">
            <h3>Total Budget</h3>
            <p className="amount">{formatCurrency(budgetSummary.totalBudget)}</p>
          </div>
          <div className="budget-card">
            <h3>Total Spent</h3>
            <p className={`amount ${budgetSummary.totalSpent > budgetSummary.totalBudget ? 'over-budget' : ''}`}>
              {formatCurrency(budgetSummary.totalSpent)}
            </p>
          </div>
          <div className="budget-card">
            <h3>Remaining</h3>
            <p className={`amount ${budgetSummary.remaining < 0 ? 'over-budget' : ''}`}>
              {formatCurrency(budgetSummary.remaining)}
            </p>
          </div>
        </div>
      )}

      <div className="actions">
        <button onClick={() => setShowPersonForm(true)} className="btn btn-primary">
          + Add Person
        </button>
        <button onClick={() => setShowGiftForm(true)} className="btn btn-primary">
          + Add Gift
        </button>
      </div>

      {showPersonForm && (
        <PersonForm
          onSubmit={handleCreatePerson}
          onCancel={() => setShowPersonForm(false)}
        />
      )}

      {editingPerson && (
        <PersonForm
          person={editingPerson}
          onSubmit={(name) => handleUpdatePerson(editingPerson.id, name)}
          onCancel={() => setEditingPerson(null)}
        />
      )}

      {showGiftForm && (
        <GiftForm
          persons={persons}
          selectedPersonId={selectedPersonId}
          onSubmit={handleCreateGift}
          onCancel={() => {
            setShowGiftForm(false);
            setSelectedPersonId(null);
          }}
        />
      )}

      {editingGift && (
        <GiftForm
          gift={editingGift}
          persons={persons}
          onSubmit={(description, price, personId) =>
            handleUpdateGift(editingGift.id, description, price, personId)
          }
          onCancel={() => setEditingGift(null)}
        />
      )}

      <div className="persons-grid">
        {persons.map((person) => {
          const personGifts = gifts.filter((g) => g.personId === person.id);
          return (
            <div key={person.id} className="person-card">
              <div className="person-header">
                <h2>{person.name}</h2>
                <div className="person-actions">
                  <button
                    onClick={() => {
                      setEditingPerson(person);
                      setShowPersonForm(false);
                    }}
                    className="btn btn-small"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => handleDeletePerson(person.id)}
                    className="btn btn-small btn-danger"
                  >
                    Delete
                  </button>
                </div>
              </div>
              <div className="person-total">
                Total: <strong>{formatCurrency(person.totalSpent)}</strong>
              </div>
              <div className="gifts-list">
                <h3>Gifts:</h3>
                {personGifts.length === 0 ? (
                  <p className="no-gifts">No gifts yet</p>
                ) : (
                  <ul>
                    {personGifts.map((gift) => (
                      <li key={gift.id} className="gift-item">
                        <span className="gift-description">{gift.description}</span>
                        <span className="gift-price">{formatCurrency(gift.price)}</span>
                        <div className="gift-actions">
                          <button
                            onClick={() => {
                              setEditingGift(gift);
                              setShowGiftForm(false);
                            }}
                            className="btn btn-tiny"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDeleteGift(gift.id)}
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
                    setSelectedPersonId(person.id);
                    setShowGiftForm(true);
                  }}
                  className="btn btn-small btn-secondary"
                >
                  + Add Gift
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {persons.length === 0 && (
        <div className="empty-state">
          <p>No people added yet. Start by adding a person!</p>
        </div>
      )}
    </div>
  );
}

interface PersonFormProps {
  person?: Person;
  onSubmit: (name: string) => void;
  onCancel: () => void;
}

function PersonForm({ person, onSubmit, onCancel }: PersonFormProps) {
  const [name, setName] = useState(person?.name || '');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (name.trim()) {
      onSubmit(name.trim());
      setName('');
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2>{person ? 'Edit Person' : 'Add Person'}</h2>
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
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {person ? 'Update' : 'Add'}
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

interface GiftFormProps {
  gift?: Gift;
  persons: Person[];
  selectedPersonId?: number | null;
  onSubmit: (description: string, price: number, personId: number) => void;
  onCancel: () => void;
}

function GiftForm({ gift, persons, selectedPersonId, onSubmit, onCancel }: GiftFormProps) {
  const [description, setDescription] = useState(gift?.description || '');
  const [price, setPrice] = useState(gift?.price.toString() || '');
  const [personId, setPersonId] = useState<number>(
    gift?.personId || selectedPersonId || persons[0]?.id || 0
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (description.trim() && price && personId) {
      onSubmit(description.trim(), parseFloat(price), personId);
      setDescription('');
      setPrice('');
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2>{gift ? 'Edit Gift' : 'Add Gift'}</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Person:</label>
            <select
              value={personId}
              onChange={(e) => setPersonId(Number(e.target.value))}
              required
            >
              {persons.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Description:</label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              autoFocus
            />
          </div>
          <div className="form-group">
            <label>Price:</label>
            <input
              type="number"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              min="0"
              step="0.01"
              required
            />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {gift ? 'Update' : 'Add'}
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

