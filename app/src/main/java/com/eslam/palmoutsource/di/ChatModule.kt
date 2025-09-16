package com.eslam.palmoutsource.di

import com.eslam.palmoutsource.data.datasource.ChatLocalDataSource
import com.eslam.palmoutsource.data.datasource.ChatLocalDataSourceImpl
import com.eslam.palmoutsource.data.datasource.ChatRemoteDataSource
import com.eslam.palmoutsource.data.datasource.ChatRemoteDataSourceImpl
import com.eslam.palmoutsource.data.repository.ChatRepositoryImpl
import com.eslam.palmoutsource.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for chat-related dependencies following Clean Architecture.
 * 
 * CLEAN ARCHITECTURE PRINCIPLES:
 * ✅ Domain interfaces bound to data implementations (dependency inversion)
 * ✅ Clear separation of concerns
 * ✅ Testable through interface abstractions
 * ✅ Framework-independent business logic
 * 
 * This module wires together all layers of the Clean Architecture.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {
    
    /**
     * Binds the domain repository interface to its data layer implementation.
     * This is the key to dependency inversion - domain depends on abstraction,
     * not concrete implementation.
     */
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository
    
    /**
     * Binds local data source interface to its implementation.
     * In production, this would be Room database implementation.
     */
    @Binds
    @Singleton
    abstract fun bindChatLocalDataSource(
        chatLocalDataSourceImpl: ChatLocalDataSourceImpl
    ): ChatLocalDataSource
    
    /**
     * Binds remote data source interface to its implementation.
     * In production, this would be Retrofit API implementation.
     */
    @Binds
    @Singleton
    abstract fun bindChatRemoteDataSource(
        chatRemoteDataSourceImpl: ChatRemoteDataSourceImpl
    ): ChatRemoteDataSource
}
