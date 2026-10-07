import { Link } from 'react-router-dom';
import { useShop } from '../context/ShopContext';
import ProductCard from '../components/ProductCard';
export default function Home() { const {products} = useShop(); return <><section className="hero rounded-4 p-4 p-md-5 mb-5"><span className="badge bg-danger mb-3">SPORTX CHILE</span><h1 className="display-5 fw-bold">Entrena con fuerza.<br/>Supera tus límites.</h1><p className="lead">Equipamiento para boxeo, fitness y tu próximo desafío.</p><Link to="/productos" className="btn btn-danger btn-lg">Explorar productos</Link></section><h2 className="h3 mb-4">Productos destacados</h2><div className="row g-4">{products.slice(0,3).map(p => <ProductCard key={p.id} product={p}/>)}</div></>; }
