import React, { useState } from 'react';
import './ProductsPage.css';

const categories = [
  { id: "100", name: "Category 1" },
  { id: "200", name: "Category 2" },
  { id: "300", name: "Category 3" },
  { id: "400", name: "Category 4" },
];

const products = [
    { itemId: "100 00001", description: "Paracetamol", packageType: "box", units: "caps", unitsInPack: 20, price: 0.10, availability: 10345 },
    { itemId: "100 00002", description: "Aspirin", packageType: "box", units: "caps", unitsInPack: 20, price: 0.50, availability: 12453 },
    { itemId: "100 00003", description: "Analgin", packageType: "box", units: "caps", unitsInPack: 10, price: 1.20, availability: 4235 },
    { itemId: "100 00004", description: "Celebrex, caps 100 mg", packageType: "box", units: "caps", unitsInPack: 10, price: 10.00, availability: 3420 },
    { itemId: "100 00005", description: "Celebrex, caps 200 mg", packageType: "box", units: "caps", unitsInPack: 10, price: 18.50, availability: 1450 },
    { itemId: "100 00006", description: "Retin-A Tretin, 30 g", packageType: "box", units: "caps", unitsInPack: 20, price: 25.00, availability: 2013 },
    { itemId: "100 00007", description: "Lipitor TB, 20 mg", packageType: "box", units: "caps", unitsInPack: 30, price: 15.50, availability: 1562 },
    { itemId: "100 00008", description: "Claritin CR, 60g", packageType: "box", units: "caps", unitsInPack: 20, price: 19.50, availability: 2540 },

    { itemId: "200 00004", description: "Iodine tincture", packageType: "bottle", units: "ml", unitsInPack: 100, price: 0.30, availability: 2213 },
    { itemId: "200 00005", description: "Rhynol", packageType: "bottle", units: "ml", unitsInPack: 200, price: 2.50, availability: 1908 },

    { itemId: "300 00001", description: "Ospen", packageType: "box", units: "caps", unitsInPack: 20, price: 10.50, availability: 809 },
    { itemId: "300 00002", description: "Amopen", packageType: "box", units: "caps", unitsInPack: 30, price: 15.00, availability: 1340 },

    { itemId: "400 00001", description: "Vitamin C", packageType: "box", units: "caps", unitsInPack: 30, price: 1.20, availability: 3258 },
    { itemId: "400 00002", description: "Vitamin B12", packageType: "box", units: "caps", unitsInPack: 30, price: 1.30, availability: 2673 },
]

function ProductsPage() {
    const [selectedCategory, setSelectedCategory] = useState('100');
    const filtered = products.filter(p => p.itemId.startsWith(selectedCategory))
  return (
    <div className="products-page">
        <div className="products-sidebar">
            <h2 className="sidebar-title">Categories</h2>
            <ul className="category-list">
                {categories.map(c => (
                    <li
                    key={c.id}
                    className={"category-item " + (selectedCategory === c.id ? "active" : "")}
                    onClick={() => setSelectedCategory(c.id)}>
                        {c.name}
                    </li>
                ))}
            </ul>
        </div>
        <div className="products-main">
            <h2 className="products-main-title">{categories.find(c => c.id === selectedCategory)?.name}</h2>
            <div className="products-grid">
                {filtered.map(p => (
                    <div key={p.itemId} className="product-card">
                        <h3>{p.description}</h3>
                        <p>{p.itemId}</p>
                        <p>{p.packageType}, {p.unitsInPack} {p.units}</p>
                        <p>£{p.price.toFixed(2)}</p>
                        <p>{p.availability} packs in stock</p>
                        <button> Add to Cart</button>
                        </div>
                ))}
            </div>
        </div>
    </div>
  );
}

export default ProductsPage;