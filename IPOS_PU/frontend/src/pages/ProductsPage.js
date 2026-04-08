import React, { useState, useEffect } from 'react';
import { useCart } from '../context/CartContext';
import './ProductsPage.css';

const categories = [
  { id: "100", name: "Category 1" },
  { id: "200", name: "Category 2" },
  { id: "300", name: "Category 3" },
  { id: "400", name: "Category 4" },
];

function ProductsPage() {
    const [selectedCategory, setSelectedCategory] = useState("100");
    const [search, setSearch] = useState("");
    const [products, setProducts] = useState([]);
    const { addToCart } = useCart();

    useEffect(() => {
    fetch(`http://localhost:8080/api/products?category=${selectedCategory}`)
        .then(res => res.json())
        .then(data => {
            console.log(data);
            setProducts(Array.isArray(data) ? data : []);
        })
        .catch(err => {
            console.error("Fetch error:", err);
            setProducts([]);
        });
}, [selectedCategory]);

    const filtered = products.filter(p =>
        p.description.toLowerCase().includes(search.toLowerCase())
    );

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
            <div className="products-main-header">
            <h2 className="products-main-title">{categories.find(c => c.id === selectedCategory)?.name}</h2>
            <div className="products-search-wrapper">
            <input
            type="text"
            className="products-search"
            placeholder="Search products"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            />
           <button className="products-search-btn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="11" cy="11" r="8"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
            </button>
            </div>
            </div>

            <div className="products-grid">
                {filtered.map(p => (
                    <div key={p.itemId} className="product-card">
                        <h3>{p.description}</h3>
                        <p className="product-id">ID: {p.itemId}</p>
                        <p className="product-detail">Package: {p.packageType} ({p.unitsInPack} units/pack)</p>
                        <p className="product-price">£{p.price.toFixed(2)} per pack</p>
                        <p className="product-stock">✓ {p.stockQuantity} packs in stock</p>
                        <button onClick={() => addToCart(p.productId, p.description, p.price)}>Add to Cart</button>
                        </div>
                ))}
            </div>
        </div>
    </div>
  );
}

export default ProductsPage;