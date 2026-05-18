import React from 'react'
import clsx from 'clsx'

export default function LoadingSpinner({ overlay = false, size = 'md' }) {
  const sizes = { sm: 'w-4 h-4', md: 'w-8 h-8', lg: 'w-12 h-12' }

  const spinner = (
    <div className={clsx('border-2 border-gray-200 border-t-primary-600 rounded-full animate-spin', sizes[size])} />
  )

  if (overlay) {
    return (
      <div className="absolute inset-0 flex items-center justify-center bg-white/70 z-10 rounded-xl">
        {spinner}
      </div>
    )
  }

  return (
    <div className="flex items-center justify-center py-12">
      {spinner}
    </div>
  )
}
