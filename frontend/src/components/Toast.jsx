import React from "react";

export default function Toast({ message, type = "success" }) {
  if (!message) return null;

  const bgColor = {
    success: "bg-green-600",
    error: "bg-red-600",
    info: "bg-blue-600",
  }[type];

  return (
    <div className="fixed top-6 left-1/2 transform -translate-x-1/2 z-50 pointer-events-none">
      <div
        className={`pointer-events-auto px-6 py-4 rounded-xl text-white shadow-xl text-center max-w-xl w-full mx-4 ${bgColor}`}
      >
        <span className="text-lg font-medium">{message}</span>
      </div>
    </div>
  );
}



