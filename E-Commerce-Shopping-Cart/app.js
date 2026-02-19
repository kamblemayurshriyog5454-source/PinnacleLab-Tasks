const baseProducts=[
  {
    id:1,cat:"audio",
    title:"Wireless Headphones",price:89.99,rating:4.7,
    img:"assets/headphones-1.svg",
    images:[
      "assets/headphones-1.svg",
      "assets/headphones-2.svg"
    ],
    desc:"Immerse yourself in rich, wireless audio with low-latency playback and soft‑touch ear cushions for all‑day comfort.",
    features:["Bluetooth 5.3","Active Noise Cancellation","30h battery","Fast charge 10min→5h"]
  },
  {
    id:2,cat:"wearables",
    title:"Smart Watch",price:129.0,rating:4.5,
    img:"assets/watch-1.svg",
    images:[
      "assets/watch-1.svg",
      "assets/watch-2.svg"
    ],
    desc:"Track health metrics, receive notifications, and control music with a vibrant AMOLED display and long battery life.",
    features:["AMOLED display","Heart rate & SpO2","GPS","7‑day battery"]
  },
  {
    id:3,cat:"accessories",
    title:"Gaming Keyboard",price:59.5,rating:4.4,
    img:"assets/keyboard-1.svg",
    images:[
      "assets/keyboard-1.svg",
      "assets/keyboard-2.svg"
    ],
    desc:"Tactile mechanical keys with RGB lighting and anti‑ghosting deliver precise inputs for competitive gaming.",
    features:["Mechanical switches","RGB lighting","Anti‑ghosting","Detachable cable"]
  },
  {
    id:4,cat:"audio",
    title:"Bluetooth Speaker",price:49.0,rating:4.6,
    img:"assets/speaker-1.svg",
    images:[
      "assets/speaker-1.svg",
      "assets/speaker-2.svg"
    ],
    desc:"Portable speaker with deep bass and IPX7 water resistance for outdoor adventures and parties.",
    features:["IPX7 waterproof","Deep bass","Stereo pair","12h playtime"]
  },
  {
    id:5,cat:"cameras",
    title:"Action Camera",price:199.0,rating:4.3,
    img:"assets/camera-1.svg",
    images:[
      "assets/camera-1.svg",
      "assets/camera-2.svg"
    ],
    desc:"Shoot 4K stabilized footage with rugged design and wide accessory support for every sport.",
    features:["4K60 video","Electronic stabilization","Waterproof housing","Wide accessories"]
  },
  {
    id:6,cat:"laptops",
    title:"Laptop Stand",price:34.99,rating:4.2,
    img:"assets/stand-1.svg",
    images:[
      "assets/stand-1.svg",
      "assets/stand-2.svg"
    ],
    desc:"Ergonomic aluminum stand improves posture and cooling with adjustable height and anti‑slip pads.",
    features:["Adjustable height","Aluminum build","Anti‑slip pads","Improved cooling"]
  }
];
const products=Array.from({length:24},(_,i)=>{
  const b=baseProducts[i%baseProducts.length];
  return {...b,id:i+1,price:parseFloat((b.price+(i%5)*3).toFixed(2)),rating:b.rating-(i%3)*0.1};
});
const grid=document.getElementById("productGrid");
const cartDrawer=document.getElementById("cartDrawer");
const cartCount=document.getElementById("cartCount");
const cartItemsEl=document.getElementById("cartItems");
const cartSubtotalEl=document.getElementById("cartSubtotal");
const searchInput=document.getElementById("searchInput");
const sortSelect=document.getElementById("sortSelect");
const ratingSelect=document.getElementById("ratingSelect");
const priceRange=document.getElementById("priceRange");
const priceLabel=document.getElementById("priceLabel");
const suggest=document.getElementById("searchSuggest");
const loadMoreBtn=document.getElementById("loadMoreBtn");
const chips=document.querySelectorAll(".chip");
const yearEl=document.getElementById("year");
const cartToggle=document.getElementById("cartToggle");
const cartClose=document.getElementById("cartClose");
const checkoutBtn=document.getElementById("checkoutBtn");
const modal=document.getElementById("quickView");
const modalClose=document.getElementById("modalClose");
const modalImg=document.getElementById("modalImg");
const modalTitle=document.getElementById("modalTitle");
const modalPrice=document.getElementById("modalPrice");
const modalRating=document.getElementById("modalRating");
const modalAdd=document.getElementById("modalAdd");
const detailBuy=document.getElementById("detailBuy");
const toastContainer=document.getElementById("toastContainer");
const detail=document.getElementById("productDetail");
const detailClose=document.getElementById("detailClose");
const detailMain=document.getElementById("detailMain");
const detailThumbs=document.getElementById("detailThumbs");
const detailChangeImage=document.getElementById("detailChangeImage");
const detailFile=document.getElementById("detailFile");
const detailUpload=document.getElementById("detailUpload");
const detailTitle=document.getElementById("detailTitle");
const detailPrice=document.getElementById("detailPrice");
const detailRating=document.getElementById("detailRating");
const detailDesc=document.getElementById("detailDesc");
const detailFeatures=document.getElementById("detailFeatures");
const detailAdd=document.getElementById("detailAdd");
const checkoutModal=document.getElementById("checkoutModal");
const summaryItems=document.getElementById("summaryItems");
const summaryTotal=document.getElementById("summaryTotal");
const placeOrderBtn=document.getElementById("placeOrderBtn");
const cancelCheckoutBtn=document.getElementById("cancelCheckoutBtn");
const orderPlacedModal=document.getElementById("orderPlacedModal");
const orderIdEl=document.getElementById("orderId");
const orderItemsCountEl=document.getElementById("orderItemsCount");
const orderPaymentEl=document.getElementById("orderPayment");
const orderTotalEl=document.getElementById("orderTotal");
const orderContinueBtn=document.getElementById("orderContinueBtn");
const ordersModal=document.getElementById("ordersModal");
const ordersList=document.getElementById("ordersList");
const ordersLink=document.getElementById("ordersLink");
const ordersClose=document.getElementById("ordersClose");
const addrName=document.getElementById("addrName");
const addrPhone=document.getElementById("addrPhone");
const addrLine1=document.getElementById("addrLine1");
const addrLine2=document.getElementById("addrLine2");
const addrCity=document.getElementById("addrCity");
const addrPin=document.getElementById("addrPin");
let cart=[];
let wishlist=JSON.parse(localStorage.getItem("wishlist")||"[]");
let visible=9;
let currentCat="all";
let currentList=products.slice();
let imageOverrides=JSON.parse(localStorage.getItem("imageOverrides")||"{}");
let checkoutItems=[];
let checkoutOrigin="cart";
yearEl.textContent=new Date().getFullYear();
function renderProducts(list){
  grid.innerHTML="";
  list.forEach(p=>{
    const el=document.createElement("article");
    el.className="product";
    el.innerHTML=`
      <img class="product-media" alt="${p.title}" src="${imageOverrides[p.id]||p.img}"/>
      <div class="product-body">
        <h3 class="product-title">${p.title}</h3>
        <div class="product-actions">
          <button class="heart ${wishlist.some(w=>w===p.id)?"active":""}" data-act="wish" data-id="${p.id}">♥</button>
          <button class="quick" data-act="quick" data-id="${p.id}">👁</button>
        </div>
        <div class="price-row">
          <span class="price">$${p.price.toFixed(2)}</span>
          <span class="rating">★ ${p.rating}</span>
        </div>
        <div style="margin-top:12px;display:flex;gap:10px">
          <button class="btn btn-primary" data-id="${p.id}">Add to Cart</button>
          <button class="btn btn-secondary" data-act="details" data-id="${p.id}">Details</button>
          <button class="btn btn-ghost" data-act="buy" data-id="${p.id}">Buy Now</button>
        </div>
      </div>
    `;
    grid.appendChild(el);
    const imgEl=el.querySelector(".product-media");
    imgEl.onerror=()=>{imgEl.src=p.img;};
  });
}
function applyFilters(){
  const q=(searchInput.value||"").toLowerCase().trim();
  let list=products.filter(p=>p.title.toLowerCase().includes(q));
  if(currentCat!=="all") list=list.filter(p=>p.cat===currentCat);
  const minRating=parseFloat(ratingSelect.value||"0");
  list=list.filter(p=>p.rating>=minRating);
  const maxPrice=parseFloat(priceRange.value||"250");
  list=list.filter(p=>p.price<=maxPrice);
  switch(sortSelect.value){
    case "priceAsc": list=list.sort((a,b)=>a.price-b.price);break;
    case "priceDesc": list=list.sort((a,b)=>b.price-a.price);break;
    default: list=list.sort((a,b)=>b.rating-a.rating);
  }
  currentList=list.slice();
  renderProducts(currentList.slice(0,visible));
  loadMoreBtn.style.display=visible<currentList.length?"block":"none";
}
function setCartState(next){
  cart=next;
  const count=cart.reduce((n,i)=>n+i.qty,0);
  cartCount.textContent=count;
  cartItemsEl.innerHTML="";
  cart.forEach(i=>{
    const row=document.createElement("div");
    row.className="cart-item";
    row.innerHTML=`
      <img alt="${i.title}" src="${i.img}"/>
      <div class="cart-item-title">${i.title}</div>
      <div class="qty">
        <button data-act="dec" data-id="${i.id}">−</button>
        <span>${i.qty}</span>
        <button data-act="inc" data-id="${i.id}">+</button>
      </div>
      <div style="font-weight:700">$${(i.price*i.qty).toFixed(2)}</div>
      <button class="icon-btn" data-act="remove" data-id="${i.id}">×</button>
    `;
    cartItemsEl.appendChild(row);
  });
  const subtotal=cart.reduce((n,i)=>n+i.price*i.qty,0);
  cartSubtotalEl.textContent=`$${subtotal.toFixed(2)}`;
  localStorage.setItem("cart",JSON.stringify(cart));
}
function addToCart(id){
  const p=products.find(x=>x.id===id);
  const exists=cart.find(x=>x.id===id);
  if(exists){setCartState(cart.map(x=>x.id===id?{...x,qty:x.qty+1}:x));}
  else{setCartState([...cart,{...p,qty:1}]);}
  showToast(`${p.title} added to cart`);
}
function updateQty(id,act){
  if(act==="inc"){setCartState(cart.map(x=>x.id===id?{...x,qty:x.qty+1}:x));return;}
  if(act==="dec"){
    const item=cart.find(x=>x.id===id);
    if(!item)return;
    if(item.qty<=1){setCartState(cart.filter(x=>x.id!==id));}
    else{setCartState(cart.map(x=>x.id===id?{...x,qty:x.qty-1}:x));}
    return;
  }
  if(act==="remove"){setCartState(cart.filter(x=>x.id!==id));}
}
function toggleWish(id){
  const has=wishlist.some(w=>w===id);
  wishlist=has?wishlist.filter(w=>w!==id):[...wishlist,id];
  localStorage.setItem("wishlist",JSON.stringify(wishlist));
  applyFilters();
  const p=products.find(x=>x.id===id);
  showToast(has?`${p.title} removed from wishlist`:`${p.title} added to wishlist`);
}
function openQuick(id){
  const p=products.find(x=>x.id===id);
  if(!p)return;
  modalImg.src=p.img;
  modalTitle.textContent=p.title;
  modalPrice.textContent=`$${p.price.toFixed(2)}`;
  modalRating.textContent=`★ ${p.rating}`;
  modal.dataset.id=id;
  modal.classList.add("show");
}
function showToast(msg){
  const t=document.createElement("div");
  t.className="toast";
  t.textContent=msg;
  toastContainer.appendChild(t);
  setTimeout(()=>{t.remove();},2500);
}
function flyToCart(imgEl){
  const rect=imgEl.getBoundingClientRect();
  const cartRect=cartToggle.getBoundingClientRect();
  const clone=imgEl.cloneNode(true);
  clone.style.position="fixed";
  clone.style.left=`${rect.left}px`;
  clone.style.top=`${rect.top}px`;
  clone.style.width=`${rect.width}px`;
  clone.style.zIndex="80";
  clone.style.transition="all .6s ease";
  document.body.appendChild(clone);
  requestAnimationFrame(()=>{
    clone.style.left=`${cartRect.left}px`;
    clone.style.top=`${cartRect.top}px`;
    clone.style.width="24px";
    clone.style.opacity="0.3";
  });
  setTimeout(()=>clone.remove(),650);
}
grid.addEventListener("click",e=>{
  const btn=e.target.closest("button");
  if(!btn)return;
  const id=parseInt(btn.dataset.id,10);
  const act=btn.dataset.act;
  if(act==="wish"){toggleWish(id);return;}
  if(act==="quick"){openQuick(id);return;}
  if(act==="details"){openDetail(id);return;}
  if(act==="buy"){openCheckout([{...products.find(x=>x.id===id),qty:1}],"single");return;}
  if(!Number.isNaN(id)){
    const card=e.target.closest(".product");
    const img=card?.querySelector(".product-media");
    if(img) flyToCart(img);
    addToCart(id);
  }
});
cartItemsEl.addEventListener("click",e=>{
  const btn=e.target.closest("button");
  if(!btn)return;
  const id=parseInt(btn.dataset.id,10);
  const act=btn.dataset.act;
  if(id && act) updateQty(id,act);
});
cartToggle.addEventListener("click",()=>{
  cartDrawer.classList.toggle("active");
});
cartClose.addEventListener("click",()=>{
  cartDrawer.classList.remove("active");
});
checkoutBtn.addEventListener("click",()=>{
  if(!cart.length){showToast("Your cart is empty");return;}
  openCheckout(cart,"cart");
});
function updateSuggest(){
  const q=(searchInput.value||"").toLowerCase().trim();
  if(!q){suggest.classList.remove("show");suggest.innerHTML="";return;}
  const items=products.filter(p=>p.title.toLowerCase().includes(q)).slice(0,5);
  suggest.innerHTML=items.map(p=>`<div class="suggest-item" data-id="${p.id}">${p.title}</div>`).join("");
  suggest.classList.add("show");
}
searchInput.addEventListener("input",()=>{updateSuggest();applyFilters();});
suggest.addEventListener("click",e=>{
  const it=e.target.closest(".suggest-item");
  if(!it)return;
  const id=parseInt(it.dataset.id,10);
  openQuick(id);
  suggest.classList.remove("show");
});
sortSelect.addEventListener("change",applyFilters);
document.getElementById("learnBtn").addEventListener("click",()=>{
  window.scrollTo({top:document.getElementById("products").offsetTop-40,behavior:"smooth"});
});
ratingSelect.addEventListener("change",applyFilters);
priceRange.addEventListener("input",()=>{
  priceLabel.textContent=`Max $${parseFloat(priceRange.value).toFixed(0)}`;
  applyFilters();
});
chips.forEach(c=>{
  c.addEventListener("click",()=>{
    chips.forEach(x=>x.classList.remove("active"));
    c.classList.add("active");
    currentCat=c.dataset.cat;
    applyFilters();
  });
});
loadMoreBtn.addEventListener("click",()=>{
  visible+=9;
  applyFilters();
});
modalClose.addEventListener("click",()=>modal.classList.remove("show"));
modal.addEventListener("click",e=>{if(e.target===modal)modal.classList.remove("show");});
modalAdd.addEventListener("click",()=>{
  const id=parseInt(modal.dataset.id,10);
  if(id){addToCart(id);modal.classList.remove("show");}
});
function openDetail(id){
  const p=products.find(x=>x.id===id);
  if(!p)return;
  detailTitle.textContent=p.title;
  detailPrice.textContent=`$${p.price.toFixed(2)}`;
  detailRating.textContent=`★ ${p.rating}`;
  detailDesc.textContent=p.desc;
  detailFeatures.innerHTML=p.features.map(f=>`<li>${f}</li>`).join("");
  detailThumbs.innerHTML="";
  const gallery=[imageOverrides[p.id]||p.images[0],...p.images.slice(1)];
  gallery.forEach((src,idx)=>{
    const im=document.createElement("img");
    im.src=src;
    im.onerror=()=>{im.src=p.img;};
    if(idx===0){im.classList.add("active");detailMain.src=src;}
    im.addEventListener("click",()=>{
      detailThumbs.querySelectorAll("img").forEach(x=>x.classList.remove("active"));
      im.classList.add("active");
      detailMain.src=src;
    });
    detailThumbs.appendChild(im);
  });
  detailMain.onerror=()=>{detailMain.src=p.img;};
  detail.dataset.id=id;
  detail.classList.add("show");
}
detailClose.addEventListener("click",()=>detail.classList.remove("show"));
detail.addEventListener("click",e=>{if(e.target===detail)detail.classList.remove("show");});
detailAdd.addEventListener("click",()=>{
  const id=parseInt(detail.dataset.id,10);
  if(id){addToCart(id);detail.classList.remove("show");}
});
detailBuy.addEventListener("click",()=>{
  const id=parseInt(detail.dataset.id,10);
  if(id){openCheckout([{...products.find(x=>x.id===id),qty:1}],"single");detail.classList.remove("show");}
});
detailChangeImage.addEventListener("click",()=>{
  const id=parseInt(detail.dataset.id,10);
  const url=prompt("Paste image URL for this product:");
  if(!url)return;
  imageOverrides[id]=url;
  localStorage.setItem("imageOverrides",JSON.stringify(imageOverrides));
  detailMain.src=url;
  applyFilters();
});
detailUpload.addEventListener("click",()=>detailFile.click());
detailFile.addEventListener("change",async ()=>{
  const id=parseInt(detail.dataset.id,10);
  const file=detailFile.files?.[0];
  if(!file||!id)return;
  const fd=new FormData();
  fd.append("file",file);
  try{
    const res=await fetch("http://localhost:8081/upload",{method:"POST",body:fd});
    if(!res.ok) throw new Error("Upload failed");
    const data=await res.json();
    imageOverrides[id]=data.path;
    localStorage.setItem("imageOverrides",JSON.stringify(imageOverrides));
    detailMain.src=data.path;
    applyFilters();
    showToast("Image saved to assets");
  }catch(e){
    showToast("Upload failed");
  }finally{
    detailFile.value="";
  }
});
function openCheckout(items,origin){
  checkoutItems=items.map(i=>({id:i.id,title:i.title,price:i.price,qty:i.qty,img:imageOverrides[i.id]||i.img}));
  checkoutOrigin=origin;
  summaryItems.innerHTML="";
  let total=0;
  checkoutItems.forEach(i=>{
    total+=i.price*i.qty;
    const row=document.createElement("div");
    row.className="row";
    row.innerHTML=`<img src="${i.img}" alt=""><div style="flex:1">${i.title} × ${i.qty}</div><div style="font-weight:700">$${(i.price*i.qty).toFixed(2)}</div>`;
    summaryItems.appendChild(row);
  });
  summaryTotal.textContent=`$${total.toFixed(2)}`;
  checkoutModal.classList.add("show");
}
cancelCheckoutBtn.addEventListener("click",()=>checkoutModal.classList.remove("show"));
placeOrderBtn.addEventListener("click",()=>{
  const name=(addrName.value||"").trim();
  const line1=(addrLine1.value||"").trim();
  if(!name||!line1){showToast("Enter name and address");return;}
  const pay=document.querySelector('input[name="payMethod"]:checked')?.value||"cod";
  const total=checkoutItems.reduce((n,i)=>n+i.price*i.qty,0);
  const id=`EC-${Date.now().toString().slice(-8)}`;
  const order={id,items:checkoutItems,total,payment:pay,address:{name,phone:addrPhone.value,line1, line2:addrLine2.value,city:addrCity.value,pin:addrPin.value},date:Date.now()};
  const orders=JSON.parse(localStorage.getItem("orders")||"[]");
  orders.push(order);
  localStorage.setItem("orders",JSON.stringify(orders));
  orderIdEl.textContent=id;
  orderItemsCountEl.textContent=`${checkoutItems.length}`;
  orderPaymentEl.textContent=pay.toUpperCase();
  orderTotalEl.textContent=`$${total.toFixed(2)}`;
  checkoutModal.classList.remove("show");
  orderPlacedModal.classList.add("show");
  if(checkoutOrigin==="cart"){setCartState([]);}
});
orderContinueBtn.addEventListener("click",()=>{
  orderPlacedModal.classList.remove("show");
});
function getOrders(){return JSON.parse(localStorage.getItem("orders")||"[]");}
function openOrders(){
  const orders=getOrders().sort((a,b)=>b.date-a.date);
  ordersList.innerHTML="";
  if(!orders.length){
    const empty=document.createElement("div");
    empty.className="order-row";
    empty.textContent="No orders yet.";
    ordersList.appendChild(empty);
  }else{
    orders.forEach(o=>{
      const row=document.createElement("div");
      row.className="order-row";
      const date=new Date(o.date).toLocaleString();
      row.innerHTML=`
        <div class="order-row-head">
          <div class="order-row-meta">
            <strong>${o.id}</strong>
            <span>${date}</span>
            <span>${o.payment.toUpperCase()}</span>
          </div>
          <div style="display:flex;gap:8px;align-items:center">
            <strong>$${o.total.toFixed(2)}</strong>
            <button class="btn btn-secondary" data-act="toggle" data-id="${o.id}">View</button>
            <button class="btn btn-primary" data-act="reorder" data-id="${o.id}">Reorder</button>
          </div>
        </div>
        <div class="order-items" id="items-${o.id}">
          ${o.items.map(i=>`
            <div class="item">
              <img src="${i.img}" alt="">
              <div style="flex:1">${i.title} × ${i.qty}</div>
              <div style="font-weight:700">$${(i.price*i.qty).toFixed(2)}</div>
            </div>
          `).join("")}
          <div style="margin-top:8px;color:#cbd5e1">
            Deliver to: ${o.address.name}, ${o.address.line1} ${o.address.line2||""}, ${o.address.city||""} ${o.address.pin||""}
          </div>
        </div>
      `;
      ordersList.appendChild(row);
    });
  }
  ordersModal.classList.add("show");
}
ordersLink.addEventListener("click",(e)=>{e.preventDefault();openOrders();});
ordersClose.addEventListener("click",()=>ordersModal.classList.remove("show"));
ordersModal.addEventListener("click",(e)=>{if(e.target===ordersModal)ordersModal.classList.remove("show");});
ordersList.addEventListener("click",(e)=>{
  const btn=e.target.closest("button");
  if(!btn)return;
  const id=btn.dataset.id;
  const act=btn.dataset.act;
  const orders=getOrders();
  const order=orders.find(x=>x.id===id);
  if(act==="toggle"){
    const el=document.getElementById(`items-${id}`);
    el.classList.toggle("show");
  }
  if(act==="reorder"&&order){
    const items=order.items.map(i=>({id:i.id,title:i.title,price:i.price,img:i.img,qty:i.qty}));
    setCartState(items);
    cartDrawer.classList.add("active");
    showToast("Items added to cart");
  }
});
const savedCart=JSON.parse(localStorage.getItem("cart")||"[]");
if(savedCart.length){setCartState(savedCart);}else{setCartState([]);}
applyFilters();
