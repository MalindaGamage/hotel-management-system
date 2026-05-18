import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import DataTable from '../../components/common/DataTable'

const COLUMNS = [
  { key: 'name',  header: 'Name'  },
  { key: 'email', header: 'Email' },
  { key: 'role',  header: 'Role'  },
]
const DATA = [
  { id: 1, name: 'Alice', email: 'alice@test.com', role: 'Admin' },
  { id: 2, name: 'Bob',   email: 'bob@test.com',   role: 'Staff' },
]

describe('DataTable', () => {
  it('renders column headers', () => {
    render(<DataTable columns={COLUMNS} data={[]} />)
    expect(screen.getByText('Name')).toBeInTheDocument()
    expect(screen.getByText('Email')).toBeInTheDocument()
    expect(screen.getByText('Role')).toBeInTheDocument()
  })

  it('renders data rows', () => {
    render(<DataTable columns={COLUMNS} data={DATA} />)
    expect(screen.getByText('Alice')).toBeInTheDocument()
    expect(screen.getByText('bob@test.com')).toBeInTheDocument()
  })

  it('shows empty state message when no data', () => {
    render(<DataTable columns={COLUMNS} data={[]} emptyMessage="Nothing here" />)
    expect(screen.getByText('Nothing here')).toBeInTheDocument()
  })

  it('calls onRowClick with the row when clicked', async () => {
    const handler = vi.fn()
    render(<DataTable columns={COLUMNS} data={DATA} onRowClick={handler} />)
    await userEvent.click(screen.getByText('Alice'))
    expect(handler).toHaveBeenCalledWith(DATA[0])
  })
})
