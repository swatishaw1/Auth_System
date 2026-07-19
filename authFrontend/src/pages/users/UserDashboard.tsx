import React from 'react'

function UserDashboard() {
    return (
        <div className='p-10 flex flex-col items-center'>
            <h1 className='text-2xl font-semibold'> Welcome to User Dashboard</h1>
            <p className="mt-2 text-muted-foreground">
                Manage your profile, access secure resources, track activity,
                update account settings, and explore all available platform
                features from one centralized dashboard.
            </p>
        </div>
    )
}

export default UserDashboard;