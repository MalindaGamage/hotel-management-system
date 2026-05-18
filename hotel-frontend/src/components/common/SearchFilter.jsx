import React, { useState, useEffect, useCallback } from 'react'
import { Search } from 'lucide-react'

function useDebounce(value, delay = 300) {
  const [dv, setDv] = useState(value)
  useEffect(() => {
    const t = setTimeout(() => setDv(value), delay)
    return () => clearTimeout(t)
  }, [value, delay])
  return dv
}

export default function SearchFilter({ onSearch, placeholder = 'Search...', filters = [], className }) {
  const [query, setQuery] = useState('')
  const debouncedQuery = useDebounce(query, 350)

  useEffect(() => { onSearch?.(debouncedQuery) }, [debouncedQuery, onSearch])

  return (
    <div className={`flex flex-wrap gap-3 ${className}`}>
      <div className="relative flex-1 min-w-48">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder={placeholder}
          className="form-input pl-9"
        />
      </div>
      {filters.map(({ key, label, options, value, onChange }) => (
        <select
          key={key}
          value={value ?? ''}
          onChange={(e) => onChange(e.target.value || null)}
          className="form-select w-auto min-w-36"
        >
          <option value="">{label}</option>
          {options.map(opt => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      ))}
    </div>
  )
}
