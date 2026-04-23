package com.allergytrack.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.allergytrack.app.domain.repository.EnvironmentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EnvironmentWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
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
