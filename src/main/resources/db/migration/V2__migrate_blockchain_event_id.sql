ALTER TABLE "EventLog" DROP CONSTRAINT "EventLog_blockchainEventId_fkey";
ALTER TABLE "BlockchainEvent" ALTER COLUMN id SET DATA TYPE uuid USING id::uuid;
ALTER TABLE "EventLog" ALTER COLUMN "blockchainEventId" SET DATA TYPE uuid USING "blockchainEventId"::uuid;
ALTER TABLE "EventLog" ADD CONSTRAINT "EventLog_blockchainEventId_fkey" FOREIGN KEY ("blockchainEventId") REFERENCES "BlockchainEvent"("id");