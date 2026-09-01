package com.allergytrack.app.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.allergytrack.shared.domain.repository.EnvironmentRepository

class EnvironmentWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val environmentRepository: EnvironmentRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            environmentRepository.fetchAndSaveCurrentEnvironment()
            Result.success()
        } catch (e: SecurityException) {
            Result.failure()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "environment_worker"
    }
}
