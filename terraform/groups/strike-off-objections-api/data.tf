data "aws_iam_policy_document" "policy" {
  statement {
    sid = "AllowUseOfTheKey"

    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:DescribeKey"
    ]

    resources = [
      aws_kms_key.key.arn
    ]
  }
}
