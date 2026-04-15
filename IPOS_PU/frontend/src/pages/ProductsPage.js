import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import './ProductsPage.css';

const categories = [
  { id: "all", name: "All Products" },
  { id: "100", name: "Category 1" },
  { id: "200", name: "Category 2" },
  { id: "300", name: "Category 3" },
  { id: "400", name: "Category 4" },
];

function ProductsPage() {
    const [searchParams] = useSearchParams();
    const [selectedCategory, setSelectedCategory] = useState("all");
    const [search, setSearch] = useState(searchParams.get('search') || "");
    const [products, setProducts] = useState([]);
    const [lastUpdated, setLastUpdated] = useState(null);
    const { addToCart } = useCart();

    const fetchProducts = () => {
        const url = selectedCategory === "all" 
            ? "http://localhost:8080/api/products"
            : `http://localhost:8080/api/products?category=${selectedCategory}`;
        fetch(url)
            .then(res => res.json())
            .then(data => {
                console.log("Products fetched:", data);
                setProducts(Array.isArray(data) ? data : []);
                setLastUpdated(new Date().toLocaleTimeString());
            })
            .catch(err => {
                console.error("Fetch error:", err);
                setProducts([]);
            });
    };

    // Update search from URL query param
    useEffect(() => {
        const q = searchParams.get('search');
        if (q) setSearch(q);
    }, [searchParams]);

    // Fetch on category change
    useEffect(() => {
        fetchProducts();
    }, [selectedCategory]);

    // Auto-refresh every 10 seconds to catch CA stock updates
    useEffect(() => {
        const interval = setInterval(fetchProducts, 10000);
        return () => clearInterval(interval);
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
                <div className="products-header-right">
                    {lastUpdated && <span className="last-updated">Updated: {lastUpdated}</span>}
                    <button className="refresh-btn" onClick={fetchProducts}>Refresh</button>
                    <div className="products-search-wrapper">
                        <input
                            type="text"
                            className="products-search"
                            placeholder="Search products"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                        />
                    </div>
                </div>
            </div>
            <div className="products-grid">
            {filtered.map(product => (
            <div key={product.productId} className="product-card">
                <div className="product-info">
                <h4 className="product-title">{product.description}</h4>
                <p className="product-price">&pound;{product.price?.toFixed(2)}</p>
                <p className="product-stock">Stock: {product.stockQuantity}</p>
                <button className="product-add-btn" onClick={() => addToCart(product)}>Add to Cart</button>
                </div>
            </div>
            ))}
            </div>
        </div>
    </div>
    );
}

export default ProductsPage;
