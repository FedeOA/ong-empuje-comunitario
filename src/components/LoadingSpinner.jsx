import React from "react";

const LoadingSpinner = () => {
  return (
    <div className="flex justify-center items-center min-h-[200px]">
      <div className="w-12 h-12 border-4 border-t-empuje-green border-gray-200 rounded-full animate-spin"></div>
    </div>
  );
};

export default LoadingSpinner;
