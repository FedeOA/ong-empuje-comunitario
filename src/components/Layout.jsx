import React from "react";
import Navbar from "./Navbar";

export default function Layout({ children }) {
  return (
    <div className="flex min-h-screen">
      <Navbar />
      <main className="flex-1 bg-empuje-bg p-6 min-h-screen">
        {children}
      </main>
    </div>
  );
}
